package io.github.cruciblemc.forgegradle.tasks;

import groovy.lang.Closure;
import org.gradle.api.tasks.bundling.Jar;

public class DelayedJar extends Jar {
  private Closure<?> closure = null;

  @Override
  public void copy() {
    if (closure != null) {
      super.manifest(closure);
    }
    super.copy();
  }

  public void setManifest(Closure<?> closure) {
    this.closure = closure;
  }
}
