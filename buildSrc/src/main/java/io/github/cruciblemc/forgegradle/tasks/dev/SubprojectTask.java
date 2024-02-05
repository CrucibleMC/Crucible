package io.github.cruciblemc.forgegradle.tasks.dev;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.util.LinkedList;
import java.util.Set;

public class SubprojectTask extends DefaultTask {
  @Internal
  private String projectName;
  @Internal
  private String tasks;
  private final LinkedList<Action<Project>> configureProject = new LinkedList<>();
  @Internal
  private Action<Task> configureTask;

  @TaskAction
  public void doTask() {
    Project childProj = getProject().project(projectName);

    // configure the project
    for (Action<Project> act : configureProject) {
      if (act != null)
        act.execute(childProj);
    }

    for (String task : tasks.split(" ")) {
      Set<Task> list = childProj.getTasksByName(task, false);
      for (Task t : list) {
        if (configureTask != null)
          configureTask.execute(t);
        executeTask((DefaultTask) t);
      }
    }

    System.gc();
  }

  private void executeTask(final DefaultTask task) {
    for (Object dep : task.getTaskDependencies().getDependencies(task)) {
      executeTask((DefaultTask) dep);
    }

    if (!task.getState().getExecuted()) {
      getLogger().lifecycle(task.getPath());
      for (Action<? super Task> action : task.getActions()) {
        action.execute(task);
      }
    }
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getTasks() {
    return tasks;
  }

  public void setTasks(String tasks) {
    this.tasks = tasks;
  }

  public Action<Task> getConfigureTask() {
    return configureTask;
  }

  public void setConfigureTask(Action<Task> configureTask) {
    this.configureTask = configureTask;
  }

  public void configureProject(Action<Project> action) {
    configureProject.add(action);
  }
}
