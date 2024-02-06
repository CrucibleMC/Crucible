package io.github.cruciblemc.forgegradle.tasks;

import com.github.abrarsyed.jastyle.ASFormatter;
import com.github.abrarsyed.jastyle.constants.EnumFormatStyle;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.tasks.abstractutil.CachedTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MakeTrueSources extends CachedTask {
  // Before all gods I swear, this is the last time I touch upon regex
  private static final String NEWLINE_REGEX = "[\\s|\\t|\\r\\n]+";
  private static final String SPACE_REGEX = "[\\s]+";
  private static final Pattern COMMENT_LINE = Pattern.compile("[^\\n]*//.*" + NEWLINE_REGEX + "\\{");
  private static final Pattern COMMENT = Pattern.compile("//.*");
  private static final Pattern FORMAT_LINE = Pattern.compile(Pattern.quote(System.lineSeparator()) + "[\\s]*\\{");
  private static final Pattern ELSEIF_LINE = Pattern.compile("\\}[\\s]*else[\\s]+if[\\s]*\\(.*\\)[\\s]*\\{");
  private static final Pattern ELSE_LINE = Pattern.compile("\\}[\\s]*else[\\s]*\\{");
  private static final Pattern CONDITION = Pattern.compile("\\(.*\\)");
  private static final Pattern CLASS = Pattern.compile(".*[\\s]+class[\\s]+[^\\n]*\\{.*\\}", Pattern.DOTALL);

  @InputFile
  private DelayedFile inJar;

  @InputFile
  private DelayedFile astyleConfig;

  @OutputFile
  @Cached
  private DelayedFile outJar;

  @Internal
  private HashMap<String, String> sourceMap = new HashMap<String, String>();

  @TaskAction
  protected void doMCPStuff() throws Throwable {
    this.getLogger().lifecycle("Loading untrue sources: " + this.getInJar().getCanonicalPath());
    this.readJarAndFix(this.getInJar());

    this.getLogger().lifecycle("Cleaning source");
    this.applyMcpCleanup(this.getAstyleConfig());

    this.getLogger().lifecycle("Saving Jar: " + this.getOutJar().getCanonicalPath());
    this.saveJar(this.getOutJar());
  }

  private void readJarAndFix(final File jar) throws IOException {
    this.getProject().getLogger().lifecycle("Begin reading jar...");
    // begin reading jar
    final ZipInputStream zin = new ZipInputStream(new FileInputStream(jar));
    ZipEntry entry = null;
    String fileStr;

    while ((entry = zin.getNextEntry()) != null) {
      // no META or dirs. wel take care of dirs later.
      if (entry.getName().contains("META-INF")) {
        continue;
      }

      // resources or directories.
      if (entry.isDirectory() || !entry.getName().endsWith(".java")) {
        // NO-OP
      } else {
        // source!
        fileStr = new String(ByteStreams.toByteArray(zin), Charset.defaultCharset());

        this.sourceMap.put(entry.getName(), fileStr);
      }
    }

    zin.close();
  }

  private void applyMcpCleanup(File conf) throws IOException {
    ASFormatter formatter = new ASFormatter();
    //OptParser parser = new OptParser(formatter);
    //parser.parseOptionFile(conf);

    Reader reader;
    Writer writer;

    formatter.setFormattingStyle(EnumFormatStyle.JAVA);
    formatter.setBreakElseIfsMode(false);
    formatter.setSpaceIndentation(4);
    formatter.setClassIndent(false);
    formatter.setNamespaceIndent(false);
    formatter.setCaseIndent(true);
    formatter.setBreakClosingHeaderBracketsMode(false);
    formatter.setDeleteEmptyLinesMode(false);
    formatter.setMaxInStatementIndentLength(40);
    //TODO: Figure out this
    //formatter.setUseProperInnerClassIndenting(true);

    ArrayList<String> files = new ArrayList<String>(this.sourceMap.keySet());
    Collections.sort(files); // Just to make sure we have the same order.. shouldn't matter on anything but lets be careful.

    for (String file : files) {
      String text = this.sourceMap.get(file);

      this.getLogger().debug("Processing file: " + file);

      Matcher commentLineMatcher = COMMENT_LINE.matcher(text);
      List<String> lines = new ArrayList<String>();

      while (commentLineMatcher.find()) {
        lines.add(commentLineMatcher.group());
      }

      for (String commentLine : lines) {
        Matcher commentMatcher = COMMENT.matcher(commentLine);

        if (commentMatcher.find()) {
          String comment = commentMatcher.group();
          String newCommentLine = commentLine.replace(comment, "") + " " + comment;
          text = text.replace(commentLine, newCommentLine);
          this.getLogger().lifecycle("Fixed comment: " + comment);
        }
      }

      lines.clear();

            /*
            Matcher elseMatcher = ELSE_LINE.matcher(text);

            while(elseMatcher.find()) {
                lines.add(elseMatcher.group());
            }

            for (String elseLine : lines) {
                String newElseLine = "} else {";
                text = text.replace(elseLine, newElseLine);
            }

            lines.clear();

            Matcher elseIfMatcher = ELSEIF_LINE.matcher(text);

            while(elseIfMatcher.find()) {
                lines.add(elseIfMatcher.group());
            }

            for (String elseIfLine : lines) {
                Matcher conditionMatcher = CONDITION.matcher(elseIfLine);

                if (conditionMatcher.find()) {
                    String condition = conditionMatcher.group();
                    String newElseIf = "} else if " + condition + "{";
                    text = text.replace(elseIfLine, newElseIf);
                }
            }

            lines.clear();

            Matcher formatMatcher = FORMAT_LINE.matcher(text);

            while (formatMatcher.find()) {
                lines.add(formatMatcher.group());
            }

            for (String formatLine : lines) {
                String newFormat = " {";
                text = text.replace(formatLine, newFormat);
            }

            lines.clear();
             */

      // If this is anonymous subclass, give it some space
      // text = text.replace("};", "};" + System.lineSeparator());

      // If we have empty {} block, beautify it
      // text = text.replace("{}", "{" + System.lineSeparator() + "   System.out.println(\"NO-OP\");" + System.lineSeparator() + "}");

      // Make it twice to be sure
      for (int i = 0; i < 2; i++) {
        reader = new StringReader(text);
        writer = new StringWriter();
        formatter.format(reader, writer);
        reader.close();
        writer.flush();
        writer.close();
        text = writer.toString();
      }

      //text = text.replace("System.out.println(\"NO-OP\");", "// NO-OP");

      List<String> textLines = Lists.newArrayList(text.split("\\r?\\n"));
      textLines.removeIf(string -> string.contains("private static final String __OBFID"));
      text = textLines.stream().collect(Collectors.joining(System.lineSeparator()));

      this.sourceMap.put(file, text);
    }
  }

  private void saveJar(File output) throws IOException {
    ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(output));

    // write in sources
    for (Map.Entry<String, String> entry : this.sourceMap.entrySet()) {
      zout.putNextEntry(new ZipEntry(entry.getKey()));
      zout.write(entry.getValue().getBytes());
      zout.closeEntry();
    }

    zout.close();
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

}
