package io.github.cruciblemc.forgegradle.tasks.signum;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Manifest;

class ClassCollection {
  private final List<ClassNode> classes;
  private final Manifest manifest;
  private final Map<String, byte[]> extraFiles;

  public ClassCollection(List<ClassNode> classes, Manifest manifest, Map<String, byte[]> extraFiles) {
    this.classes = classes;
    this.manifest = manifest;
    this.extraFiles = extraFiles;
  }

  public List<ClassNode> getClasses() {
    return this.classes;
  }

  public Manifest getManifest() {
    return this.manifest;
  }

  public Map<String, byte[]> getExtraFiles() {
    return this.extraFiles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;

    ClassCollection that = (ClassCollection) o;

    if (!Objects.equals(this.classes, that.classes))
      return false;
    if (!Objects.equals(this.extraFiles, that.extraFiles))
      return false;
    return Objects.equals(this.manifest, that.manifest);
  }

  @Override
  public int hashCode() {
    int result = this.classes != null ? this.classes.hashCode() : 0;
    result = 31 * result + (this.manifest != null ? this.manifest.hashCode() : 0);
    result = 31 * result + (this.extraFiles != null ? this.extraFiles.hashCode() : 0);
    return result;
  }
}