package io.github.cruciblemc.forgegradle.tasks;

import com.juanmuscaria.uncode.ASMCodeRemover;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.gradle.delayed.DelayedFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

@Getter
@Setter
public class UncodeJarTask extends DefaultTask {
  @InputFile
  private DelayedFile inputJar;

  @OutputFile
  private DelayedFile outJar;

  @TaskAction
  public void doTask() throws IOException {
    ASMCodeRemover.removeContent(getInputJar().call().toPath(), getOutJar().call().toPath(), true);
  }
}
