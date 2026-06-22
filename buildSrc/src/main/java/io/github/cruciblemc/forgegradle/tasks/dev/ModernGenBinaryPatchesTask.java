package io.github.cruciblemc.forgegradle.tasks.dev;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.nothome.delta.Delta;
import lzma.streams.LzmaOutputStream;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.delayed.DelayedFileTree;
import org.apache.commons.compress.java.util.jar.Pack200;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.Adler32;
import java.util.zip.ZipEntry;

public class ModernGenBinaryPatchesTask extends DefaultTask {
  @InputFile
  private DelayedFile cleanClient;
  @InputFile
  private DelayedFile cleanServer;
  @InputFile
  private DelayedFile cleanMerged;
  @InputFile
  private DelayedFile dirtyJar;
  @InputFile
  private DelayedFile deobfDataLzma;
  @InputFile
  private DelayedFile srg;
  @OutputFile
  private DelayedFile outJar;

  private final List<DelayedFileTree> patchList = new ArrayList<>();
  private final HashMap<String, String> obfMapping = new HashMap<>();
  private final HashMap<String, String> srgMapping = new HashMap<>();
  private final ArrayListMultimap<String, String> innerClasses = ArrayListMultimap.create();
  private final Set<String> patchedFiles = new HashSet<>();
  private final Delta delta = new Delta();

  @TaskAction
  public void doTask() throws Exception {
    loadMappings();

    for (DelayedFileTree tree : patchList) {
      for (File patch : tree.call().getFiles()) {
        String name = patch.getName().replace(".java.patch", "");
        String obfName = srgMapping.get(name);
        patchedFiles.add(obfName);
        addInnerClasses(name, patchedFiles);
      }
    }

    HashMap<String, byte[]> runtime = new HashMap<>();
    HashMap<String, byte[]> devtime = new HashMap<>();

    createBinPatches(runtime, "client/", getCleanClient(), getDirtyJar());
    createBinPatches(runtime, "server/", getCleanServer(), getDirtyJar());
    createBinPatches(devtime, "merged/", getCleanMerged(), getDirtyJar());

    byte[] runtimedata = compress(pack200(createPatchJar(runtime)));
    byte[] devtimedata = compress(pack200(createPatchJar(devtime)));

    buildOutput(runtimedata, devtimedata);
  }

  private void addInnerClasses(String parent, Set<String> patchList) {
    for (String inner : innerClasses.get(parent)) {
      patchList.add(srgMapping.get(inner));
      addInnerClasses(inner, patchList);
    }
  }

  private void loadMappings() throws Exception {
    Splitter splitter = Splitter.on(CharMatcher.anyOf(": ")).omitEmptyStrings().trimResults();
    for (String line : Files.readAllLines(getSrg().toPath(), Charset.defaultCharset())) {
      if (!line.startsWith("CL")) {
        continue;
      }

      String[] parts = Iterables.toArray(splitter.split(line), String.class);
      obfMapping.put(parts[1], parts[2]);
      String srgName = parts[2].substring(parts[2].lastIndexOf('/') + 1);
      srgMapping.put(srgName, parts[1]);
      int innerDollar = srgName.lastIndexOf('$');
      if (innerDollar > 0) {
        String outer = srgName.substring(0, innerDollar);
        innerClasses.put(outer, srgName);
      }
    }
  }

  private void createBinPatches(HashMap<String, byte[]> patches, String root, File base, File target) throws Exception {
    try (JarFile cleanJar = new JarFile(base); JarFile dirtyJar = new JarFile(target)) {
      for (Map.Entry<String, String> entry : obfMapping.entrySet()) {
        String obf = entry.getKey();
        String srg = entry.getValue();

        if (!patchedFiles.contains(obf)) {
          continue;
        }

        JarEntry cleanEntry = cleanJar.getJarEntry(obf + ".class");
        JarEntry dirtyEntry = dirtyJar.getJarEntry(obf + ".class");

        if (dirtyEntry == null) {
          continue;
        }

        byte[] clean = cleanEntry != null ? ByteStreams.toByteArray(cleanJar.getInputStream(cleanEntry)) : new byte[0];
        byte[] dirty = ByteStreams.toByteArray(dirtyJar.getInputStream(dirtyEntry));
        byte[] diff = delta.compute(clean, dirty);

        ByteArrayDataOutput out = ByteStreams.newDataOutput(diff.length + 50);
        out.writeUTF(obf);
        out.writeUTF(obf.replace('/', '.'));
        out.writeUTF(srg.replace('/', '.'));
        out.writeBoolean(cleanEntry != null);
        if (cleanEntry != null) {
          out.writeInt(adlerHash(clean));
        }
        out.writeInt(diff.length);
        out.write(diff);

        patches.put(root + srg.replace('/', '.') + ".binpatch", out.toByteArray());
      }
    }
  }

  private int adlerHash(byte[] input) {
    Adler32 hasher = new Adler32();
    hasher.update(input);
    return (int) hasher.getValue();
  }

  private byte[] createPatchJar(HashMap<String, byte[]> patches) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (JarOutputStream jar = new JarOutputStream(out)) {
      for (Map.Entry<String, byte[]> entry : patches.entrySet()) {
        jar.putNextEntry(new JarEntry("binpatch/" + entry.getKey()));
        jar.write(entry.getValue());
      }
    }
    return out.toByteArray();
  }

  private byte[] pack200(byte[] data) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream err = System.err;
    try (JarInputStream in = new JarInputStream(new ByteArrayInputStream(data))) {
      Pack200.Packer packer = Pack200.newPacker();
      SortedMap<String, String> props = packer.properties();
      props.put(Pack200.Packer.EFFORT, "9");
      props.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.TRUE);
      props.put(Pack200.Packer.UNKNOWN_ATTRIBUTE, Pack200.Packer.PASS);

      System.setErr(new PrintStream(ByteStreams.nullOutputStream()));
      packer.pack(in, out);
    } finally {
      System.setErr(err);
    }
    return out.toByteArray();
  }

  private byte[] compress(byte[] data) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (LzmaOutputStream lzma = new LzmaOutputStream.Builder(out).useEndMarkerMode(true).build()) {
      lzma.write(data);
    }
    return out.toByteArray();
  }

  private void buildOutput(byte[] runtime, byte[] devtime) throws Exception {
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(getOutJar().toPath()));
         JarFile in = new JarFile(getDirtyJar())) {
      if (runtime != null) {
        out.putNextEntry(new JarEntry("binpatches.pack.lzma"));
        out.write(runtime);
      }

      if (devtime != null) {
        out.putNextEntry(new JarEntry("devbinpatches.pack.lzma"));
        out.write(devtime);
      }

      for (JarEntry entry : Collections.list(in.entries())) {
        if (entry.isDirectory()) {
          continue;
        }
        if (!entry.getName().endsWith(".class") || obfMapping.containsKey(entry.getName().replace(".class", ""))) {
          continue;
        }

        ZipEntry next = new ZipEntry(entry.getName());
        next.setTime(entry.getTime());
        out.putNextEntry(next);
        out.write(ByteStreams.toByteArray(in.getInputStream(entry)));
      }
    }
  }

  public File getCleanClient() {
    return cleanClient.call();
  }

  public void setCleanClient(DelayedFile cleanClient) {
    this.cleanClient = cleanClient;
  }

  public File getCleanServer() {
    return cleanServer.call();
  }

  public void setCleanServer(DelayedFile cleanServer) {
    this.cleanServer = cleanServer;
  }

  public File getCleanMerged() {
    return cleanMerged.call();
  }

  public void setCleanMerged(DelayedFile cleanMerged) {
    this.cleanMerged = cleanMerged;
  }

  public File getDirtyJar() {
    return dirtyJar.call();
  }

  public void setDirtyJar(DelayedFile dirtyJar) {
    this.dirtyJar = dirtyJar;
  }

  public void addPatchList(DelayedFileTree patchList) {
    this.patchList.add(patchList);
  }

  public File getDeobfDataLzma() {
    return deobfDataLzma.call();
  }

  public void setDeobfDataLzma(DelayedFile deobfDataLzma) {
    this.deobfDataLzma = deobfDataLzma;
  }

  public File getOutJar() {
    return outJar.call();
  }

  public void setOutJar(DelayedFile outJar) {
    this.outJar = outJar;
  }

  public File getSrg() {
    return srg.call();
  }

  public void setSrg(DelayedFile srg) {
    this.srg = srg;
  }
}
