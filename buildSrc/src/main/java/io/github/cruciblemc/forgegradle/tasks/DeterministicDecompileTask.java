package io.github.cruciblemc.forgegradle.tasks;

import com.github.abrarsyed.jastyle.ASFormatter;
import com.github.abrarsyed.jastyle.FileWildcardFilter;
import com.github.abrarsyed.jastyle.OptParser;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import groovy.lang.Closure;
import io.github.cruciblemc.forgegradle.DevConstants;
import net.minecraftforge.gradle.common.BaseExtension;
import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.extrastuff.FFPatcher;
import net.minecraftforge.gradle.extrastuff.FmlCleanup;
import net.minecraftforge.gradle.extrastuff.GLConstantFixer;
import net.minecraftforge.gradle.extrastuff.McpCleanup;
import net.minecraftforge.gradle.patching.ContextualPatch;
import net.minecraftforge.gradle.patching.ContextualPatch.HunkReport;
import net.minecraftforge.gradle.patching.ContextualPatch.PatchReport;
import net.minecraftforge.gradle.patching.ContextualPatch.PatchStatus;
import net.minecraftforge.gradle.tasks.abstractutil.CachedTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.*;
import org.gradle.process.JavaExecSpec;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static net.minecraftforge.gradle.common.Constants.EXT_NAME_MC;

public class DeterministicDecompileTask extends CachedTask {
  private static final Pattern BEFORE = Pattern.compile("(?m)((case|default).+(?:\\r\\n|\\r|\\n))(?:\\r\\n|\\r|\\n)");
  private static final Pattern AFTER = Pattern.compile("(?m)(?:\\r\\n|\\r|\\n)((?:\\r\\n|\\r|\\n)[ \\t]+(case|default))");
  @InputFile
  public DelayedFile inJar;
  @InputFile
  private DelayedFile fernFlower;
  @Internal
  private DelayedFile patch;
  @InputFile
  private DelayedFile astyleConfig;
  @Cached
  @OutputFile
  private DelayedFile outJar;
  @Internal
  private HashMap<String, String> sourceMap = new HashMap<>();
  @Internal
  private HashMap<String, byte[]> resourceMap = new HashMap<>();

  /**
   * This method outputs to the cleanSrc
   *
   * @throws Throwable Let em throw anything.. I dont care.
   */
  @TaskAction
  protected void doMCPStuff() throws Throwable {
    // define files.
    File temp = new File(this.getTemporaryDir(), this.getInJar().getName());

    InputStream forgeflower = this.getClass().getResourceAsStream("/crucible/forgeflower.jar");
    if (forgeflower == null)
      throw new IOException("Embedded forgeflower is missing and cannot be extracted!");

    File forgeFlower = new File(this.getTemporaryDir(), "forgeFlower.jar");
    java.nio.file.Files.copy(forgeflower, forgeFlower.toPath(), StandardCopyOption.REPLACE_EXISTING);

    this.getLogger().info("Decompiling Jar");
    this.decompile(this.getInJar(), this.getTemporaryDir(), forgeFlower);

    this.getLogger().info("Applying crucible fixes");
    InputStream patches = this.getClass().getResourceAsStream("/crucible/crucible-patches.zip");
    if (patches == null)
      throw new IOException("Embedded crucible-patches is missing and cannot be extracted!");
    File patchFiles = new File(this.getTemporaryDir(), "crucible-patches");
    safeUnzip(patches, patchFiles.toPath());
    this.crucibleFixJar(patchFiles, temp);

    this.getLogger().info("Loading decompiled jar");
    this.readJarAndFix(temp);

    this.saveJar(new File(this.getTemporaryDir(), this.getInJar().getName() + ".fixed.jar"));

    this.getLogger().info("Applying MCP patches");
    if (this.getPatch().isFile()) {
      this.applySingleMcpPatch(this.getPatch());
    } else {
      this.applyPatchDirectory(this.getPatch());
    }

    this.saveJar(new File(this.getTemporaryDir(), this.getInJar().getName() + ".patched.jar"));

    this.getLogger().info("Cleaning source");
    this.applyMcpCleanup(this.getAstyleConfig());

    this.getLogger().info("Saving Jar");
    this.saveJar(this.getOutJar());
  }

  // this ugly horrible terrible method does the magic of patching the 1.12 forgeflower output to the 1.7.10 equivalent
  @SuppressWarnings({"IOStreamConstructor", "deprecation"})
  private void crucibleFixJar(File patchFiles, File jar) throws Throwable {
    HashMap<String, String> sourceMap = new HashMap<>();
    HashMap<String, byte[]> resourceMap = new HashMap<>();

    final ZipInputStream zin = new ZipInputStream(new FileInputStream(jar));
    ZipEntry entry;
    String fileStr;
    while ((entry = zin.getNextEntry()) != null) {
      if (entry.getName().contains("META-INF")) {
        continue;
      }

      if (entry.isDirectory() || !entry.getName().endsWith(".java")) {
        resourceMap.put(entry.getName(), ByteStreams.toByteArray(zin));
      } else {
        fileStr = new String(ByteStreams.toByteArray(zin), Charset.defaultCharset());
        sourceMap.put(entry.getName(), fileStr);
      }
    }
    zin.close();
    try (Stream<Path> pathStream = java.nio.file.Files.walk(patchFiles.toPath())) {
      for (Path f : pathStream.filter(java.nio.file.Files::isRegularFile).collect(Collectors.toList())) {
        ContextualPatch patch = ContextualPatch.create(Files.toString(f.toFile(),
                Charset.defaultCharset()), new SrcContextProvider(sourceMap));
        this.printPatchErrors(patch.patch(false));
      }
    }

    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(jar));
    for (Map.Entry<String, byte[]> entry1 : resourceMap.entrySet()) {
      zout.putNextEntry(new ZipEntry(entry1.getKey()));
      zout.write(entry1.getValue());
      zout.closeEntry();
    }

    for (Map.Entry<String, String> entry1 : sourceMap.entrySet()) {
      zout.putNextEntry(new ZipEntry(entry1.getKey()));
      zout.write(entry1.getValue().getBytes());
      zout.closeEntry();
    }

    zout.close();
  }

  private void decompile(final File inJar, final File outJar, final File fernFlower) {
    this.getProject().javaexec(new Closure<JavaExecSpec>(this) {
      private static final long serialVersionUID = 4608694547855396167L;

      @Override
      public JavaExecSpec call() {
        JavaExecSpec exec = (JavaExecSpec) this.getDelegate();

        exec.args(
                fernFlower.getAbsolutePath(),
                "-din=1",
                "-rbr=0",
                "-dgs=1",
                "-asc=1",
                "-log=ERROR",
                inJar.getAbsolutePath(),
                outJar.getAbsolutePath()
        );

        exec.getMainClass().set("-jar");
        //exec.jvmArgs("-Dfile.encoding=" + Charset.defaultCharset());
        exec.setWorkingDir(fernFlower.getParentFile());

        exec.classpath(Constants.getClassPath());
        exec.setStandardOutput(DevConstants.getTaskLogStream(DeterministicDecompileTask.this.getProject(), DeterministicDecompileTask.this.getName() + ".log"));

        exec.setMaxHeapSize("512M");

        return exec;
      }

      @Override
      public JavaExecSpec call(Object obj) {
        return this.call();
      }
    });
  }

  private void readJarAndFix(final File jar) throws IOException {
    // begin reading jar
    final ZipInputStream zin = new ZipInputStream(new FileInputStream(jar));
    ZipEntry entry;
    String fileStr;

    BaseExtension exten = (BaseExtension) this.getProject().getExtensions().getByName(EXT_NAME_MC);
    boolean fixInterfaces = !exten.getVersion().equals("1.7.2");

    while ((entry = zin.getNextEntry()) != null) {
      // no META or dirs. wel take care of dirs later.
      if (entry.getName().contains("META-INF")) {
        continue;
      }

      // resources or directories.
      if (entry.isDirectory() || !entry.getName().endsWith(".java")) {
        this.resourceMap.put(entry.getName(), ByteStreams.toByteArray(zin));
      } else {
        // source!
        fileStr = new String(ByteStreams.toByteArray(zin), Charset.defaultCharset());

        // fix
        fileStr = FFPatcher.processFile(new File(entry.getName()).getName(), fileStr, fixInterfaces);

        this.sourceMap.put(entry.getName(), fileStr);
      }
    }

    zin.close();
  }

  private void applySingleMcpPatch(File patchFile) throws Throwable {
    ContextualPatch patch = ContextualPatch.create(Files.asCharSource(patchFile, Charset.defaultCharset()).read(), new ContextProvider(this.sourceMap));
    this.printPatchErrors(patch.patch(false));
  }

  private void printPatchErrors(List<PatchReport> errors) throws Throwable {
    boolean fuzzed = false;
    for (PatchReport report : errors) {
      if (!report.getStatus().isSuccess()) {
        this.getLogger().log(LogLevel.ERROR, "Patching failed: " + report.getTarget(), report.getFailure());

        for (HunkReport hunk : report.getHunks()) {
          if (!hunk.getStatus().isSuccess()) {
            this.getLogger().error("Hunk " + hunk.getHunkID() + " failed!");
          }
        }

        throw report.getFailure();
      } else if (report.getStatus() == PatchStatus.Fuzzed) // catch fuzzed patches
      {
        this.getLogger().log(LogLevel.INFO, "Patching fuzzed: " + report.getTarget(), report.getFailure());
        fuzzed = true;

        for (HunkReport hunk : report.getHunks()) {
          if (!hunk.getStatus().isSuccess()) {
            this.getLogger().info("Hunk " + hunk.getHunkID() + " fuzzed " + hunk.getFuzz() + "!");
          }
        }
      } else {
        this.getLogger().debug("Patch succeeded: " + report.getTarget());
      }
    }
    if (fuzzed) {
      this.getLogger().lifecycle("Patches Fuzzed!");
    }
  }

  private void applyPatchDirectory(File patchDir) throws Throwable {
    Multimap<String, File> patches = ArrayListMultimap.create();
    for (File f : Objects.requireNonNull(patchDir.listFiles(new FileWildcardFilter("*.patch")))) {
      String base = f.getName();
      patches.put(base, f);
      for (File e : Objects.requireNonNull(patchDir.listFiles(new FileWildcardFilter(base + ".*")))) {
        patches.put(base, e);
      }
    }

    for (String key : patches.keySet()) {
      ContextualPatch patch = this.findPatch(patches.get(key));
      if (patch == null) {
        this.getLogger().lifecycle("Patch not found for set: " + key); //This should never happen, but whatever
      } else {
        this.printPatchErrors(patch.patch(false));
      }
    }
  }

  private ContextualPatch findPatch(Collection<File> files) throws Throwable {
    ContextualPatch patch = null;
    for (File f : files) {
      patch = ContextualPatch.create(Files.asCharSource(f, Charset.defaultCharset()).read(), new ContextProvider(this.sourceMap));
      List<PatchReport> errors = patch.patch(true);

      boolean success = true;
      for (PatchReport rep : errors) {
        if (!rep.getStatus().isSuccess()) {
          success = false;
          break;
        }
      }
      if (success) {
        break;
      }
    }
    return patch;
  }

  private void applyMcpCleanup(File conf) throws IOException {
    ASFormatter formatter = new ASFormatter();
    OptParser parser = new OptParser(formatter);
    parser.parseOptionFile(conf);

    Reader reader;
    Writer writer;

    GLConstantFixer fixer = new GLConstantFixer();
    ArrayList<String> files = new ArrayList<>(this.sourceMap.keySet());
    Collections.sort(files); // Just to make sure we have the same order.. shouldn't matter on anything but lets be careful.

    for (String file : files) {
      String text = this.sourceMap.get(file);

      this.getLogger().debug("Processing file: " + file);

      this.getLogger().debug("processing comments");
      text = McpCleanup.stripComments(text);

      this.getLogger().debug("fixing imports comments");
      text = McpCleanup.fixImports(text);

      this.getLogger().debug("various other cleanup");
      text = McpCleanup.cleanup(text);

      this.getLogger().debug("fixing OGL constants");
      text = fixer.fixOGL(text);

      this.getLogger().debug("formatting source");
      reader = new StringReader(text);
      writer = new StringWriter();
      formatter.format(reader, writer);
      reader.close();
      writer.flush();
      writer.close();
      text = writer.toString();

      this.getLogger().debug("applying FML transformations");
      text = BEFORE.matcher(text).replaceAll("$1");
      text = AFTER.matcher(text).replaceAll("$1");
      text = FmlCleanup.renameClass(text);

      this.sourceMap.put(file, text);
    }
  }

  private void saveJar(File output) throws IOException {
    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(output));

    // write in resources
    for (Map.Entry<String, byte[]> entry : this.resourceMap.entrySet()) {
      zout.putNextEntry(new ZipEntry(entry.getKey()));
      zout.write(entry.getValue());
      zout.closeEntry();
    }

    // write in sources
    for (Map.Entry<String, String> entry : this.sourceMap.entrySet()) {
      zout.putNextEntry(new ZipEntry(entry.getKey()));
      zout.write(entry.getValue().getBytes());
      zout.closeEntry();
    }

    zout.close();
  }

  private static void safeUnzip(InputStream source, Path target) throws IOException {
    try (ZipInputStream zip = new ZipInputStream(source)) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {

        // Prevent against traversal path attacks
        Path filePath = target.resolve(entry.getName()).normalize();
        if (!filePath.startsWith(target)) {
          throw new IOException("Transversal path detected: " + entry.getName());
        }

        if (entry.isDirectory()) {
          java.nio.file.Files.createDirectories(filePath);
        } else {
          if (filePath.getParent() != null && java.nio.file.Files.notExists(filePath.getParent())) {
            java.nio.file.Files.createDirectories(filePath.getParent());
          }
          java.nio.file.Files.copy(zip, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  public HashMap<String, String> getSourceMap() {
    return this.sourceMap;
  }

  public void setSourceMap(HashMap<String, String> sourceMap) {
    this.sourceMap = sourceMap;
  }

  public File getAstyleConfig() {
    return this.astyleConfig.call();
  }

  public void setAstyleConfig(DelayedFile astyleConfig) {
    this.astyleConfig = astyleConfig;
  }

  public File getFernFlower() {
    return this.fernFlower.call();
  }

  public void setFernFlower(DelayedFile fernFlower) {
    this.fernFlower = fernFlower;
  }

  public File getInJar() {
    return this.inJar.call();
  }

  public void setInJar(DelayedFile inJar) {
    this.inJar = inJar;
  }

  public File getOutJar() {
    return this.outJar.call();
  }

  public void setOutJar(DelayedFile outJar) {
    this.outJar = outJar;
  }

  @InputFiles
  public FileCollection getPatches() {
    File patches = this.patch.call();
    if (patches.isDirectory())
      return this.getProject().fileTree(patches);
    else
      return this.getProject().files(patches);
  }

  public File getPatch() {
    return this.patch.call();
  }

  public void setPatch(DelayedFile patch) {
    this.patch = patch;
  }

  public HashMap<String, byte[]> getResourceMap() {
    return this.resourceMap;
  }

  public void setResourceMap(HashMap<String, byte[]> resourceMap) {
    this.resourceMap = resourceMap;
  }

  /**
   * A private inner class to be used with the MCPPatches only.
   */
  private static class ContextProvider implements ContextualPatch.IContextProvider {
    private final int STRIP = 1;
    private final Map<String, String> fileMap;

    public ContextProvider(Map<String, String> fileMap) {
      this.fileMap = fileMap;
    }

    private String strip(String target) {
      target = target.replace('\\', '/');
      int index = 0;
      for (int x = 0; x < this.STRIP; x++) {
        index = target.indexOf('/', index) + 1;
      }
      return target.substring(index);
    }

    @Override
    public List<String> getData(String target) {
      target = this.strip(target);

      if (this.fileMap.containsKey(target)) {
        String[] lines = this.fileMap.get(target).split("\r\n|\r|\n");
        List<String> ret = new ArrayList<String>();
        Collections.addAll(ret, lines);
        return ret;
      }

      return null;
    }

    @Override
    public void setData(String target, List<String> data) {
      this.fileMap.put(this.strip(target), Joiner.on(Constants.NEWLINE).join(data));
    }
  }

  public static class SrcContextProvider implements ContextualPatch.IContextProvider {
    private final Map<String, String> fileMap;

    private final int STRIP = 3;

    public SrcContextProvider(Map<String, String> fileMap) {
      this.fileMap = fileMap;
    }

    public String strip(String target) {
      target = target.replace('\\', '/');
      int index = 0;
      for (int x = 0; x < STRIP; x++) {
        index = target.indexOf('/', index) + 1;
      }
      return target.substring(index);
    }

    @Override
    public List<String> getData(String target) {
      target = strip(target);

      if (fileMap.containsKey(target)) {
        String[] lines = fileMap.get(target).split("\r\n|\r|\n");
        List<String> ret = new ArrayList<String>();
        Collections.addAll(ret, lines);
        return ret;
      }

      return null;
    }

    @Override
    public void setData(String target, List<String> data) {
      target = strip(target);
      fileMap.put(target, Joiner.on(Constants.NEWLINE).join(data));
    }
  }
}