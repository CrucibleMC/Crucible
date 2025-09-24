package io.github.cruciblemc.forgegradle;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.gradle.common.BaseExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DevExtension extends BaseExtension {
    @Setter
    private String fmlDir;
    @Setter
    private String forgeDir;
    @Setter
    private String bukkitDir;
    @Getter
    private String mainClass = "";
    @Getter
    private String tweakClass = "";
    @Getter
    private List<String> repos = new ArrayList<>();
    @Setter
    @Getter
    private boolean makeJavadoc = true;
    @Setter
    @Getter
    private String installerVersion = "null";

    @Setter
    @Getter
    private Action<Project> subprojects;
    @Setter
    @Getter
    private Action<Project> cleanProject;
    @Setter
    @Getter
    private Action<Project> dirtyProject;

    public DevExtension(DevBasePlugin plugin) {
        super(plugin);
    }

    public String getFmlDir() {
        return normalizeOrDefault(fmlDir);
    }

    public String getForgeDir() {
        return normalizeOrDefault(forgeDir);
    }

    public String getBukkitDir() {
        return normalizeOrDefault(bukkitDir);
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass == null ? "" : mainClass;
    }

    public void setTweakClass(String tweakClass) {
        this.tweakClass = tweakClass == null ? "" : tweakClass;
    }

    public void setRepos(List<String> repos) {
        this.repos = new ArrayList<>(repos);
    }

    public void setRepos(String... repos) {
        this.repos = new ArrayList<>(Arrays.asList(repos));
    }

    // --- Helpers ---

    private String normalizeOrDefault(String path) {
        if (path == null) {
            return project.getProjectDir().getPath().replace('\\', '/');
        }
        return path.replace('\\', '/');
    }
}