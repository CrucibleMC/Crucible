package io.github.cruciblemc.forgegradle.reobf;

import net.md_5.specialsource.Jar;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.RemapperProcessor;
import net.md_5.specialsource.repo.ClassRepo;
import net.md_5.specialsource.repo.JarRepo;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Contains features from <a href="URL#https://github.com/md-5/SpecialSource/pull/76">here</a>.
 *
 * @author MJaroslav
 */

public class JarRemapperWrapper extends JarRemapper {
  private static final int CLASS_LEN = ".class".length(); // From parent

  private boolean copyResources = true; // From parent
  private boolean copyEmptyDirectories = false; // Default value changed for this project

  public JarRemapperWrapper(RemapperProcessor preProcessor, JarMapping jarMapping, RemapperProcessor postProcessor) {
    super(preProcessor, jarMapping, postProcessor);
  }

  public JarRemapperWrapper(RemapperProcessor remapperPreprocessor, JarMapping jarMapping) {
    super(remapperPreprocessor, jarMapping);
  }

  public JarRemapperWrapper(JarMapping jarMapping) {
    super(jarMapping);
  }

  @Override
  public void setGenerateAPI(boolean generateAPI) {
    super.setGenerateAPI(generateAPI);
    this.copyResources = !generateAPI; // Copy parent private value
  }

  public void setCopyEmptyDirectories(boolean copyEmptyDirectories) {
    this.copyEmptyDirectories = copyEmptyDirectories;
  }

  @Override
  public void remapJar(Jar jar, File target) throws IOException {
    try (JarOutputStream out = new JarOutputStream(new FileOutputStream(target))) {
      ClassRepo repo = new JarRepo(jar);
      if (jar == null)
        return;
      for (String name : jar.getEntryNames()) {
        JarEntry entry;
        try (InputStream is = jar.getResource(name)) {
          byte[] data;
          if (name.endsWith(".class")) {
            // remap classes
            name = name.substring(0, name.length() - CLASS_LEN);

            data = this.remapClassFile(is, repo);
            String newName = this.map(name);

            entry = new JarEntry(newName == null ? name : newName + ".class");
          } else if (name.endsWith(".DSA") || name.endsWith(".SF")) {
            // skip signatures
            continue;
          } else {
            // copy other resources
            if (!this.copyResources) {
              continue; // unless generating an API
            }
            if (!this.copyEmptyDirectories && name.endsWith("/")) {
              continue; // Don't copy empty directories
            }
            entry = new JarEntry(name);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int n;
            byte[] b = new byte[1 << 15]; // Max class file size
            while ((n = is.read(b, 0, b.length)) != -1) {
              buffer.write(b, 0, n);
            }
            buffer.flush();
            data = buffer.toByteArray();
          }
          entry.setTime(0);
          out.putNextEntry(entry);
          out.write(data);
        }
      }
    }
  }
}
