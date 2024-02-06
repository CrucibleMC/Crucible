package io.github.cruciblemc.forgegradle.tasks.signum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Fixes bug with JRE that expects the Manifest to always be the first entry in META-INF, when in some cases, it isn't.<br/>
 * Credit to clienthax for finding this: <a href=http://bugs.java.com/view_bug.do?bug_id=4338238>http://bugs.java.com/view_bug.do?bug_id=4338238</a>
 */

class FixedJarInputStream extends JarInputStream {
  private Manifest manifest;

  public FixedJarInputStream(File file, boolean verify) throws IOException {
    super(new FileInputStream(file), verify);
    JarFile jar = new JarFile(file);
    JarEntry manifestEntry = jar.getJarEntry(JarFile.MANIFEST_NAME);
    try {
      if (manifestEntry != null) {
        this.manifest = new Manifest(jar.getInputStream(manifestEntry));
      }
    } finally {
      jar.close();
    }
  }

  @Override
  public Manifest getManifest() {
    return this.manifest;
  }
}