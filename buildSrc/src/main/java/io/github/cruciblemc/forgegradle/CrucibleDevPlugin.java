package io.github.cruciblemc.forgegradle;

import com.anatawa12.forge.gradle.separated.SeparatedLauncher;
import io.github.cruciblemc.forgegradle.tasks.DelayedJar;
import io.github.cruciblemc.forgegradle.tasks.DeterministicDecompileTask;
import io.github.cruciblemc.forgegradle.tasks.ExtractS2SRangeTask;
import io.github.cruciblemc.forgegradle.tasks.UncodeJarTask;
import io.github.cruciblemc.forgegradle.tasks.dev.GenDevProjectsTask;
import io.github.cruciblemc.forgegradle.tasks.dev.ObfuscateTask;
import io.github.cruciblemc.forgegradle.tasks.dev.SubprojectTask;
import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.tasks.ApplyS2STask;
import net.minecraftforge.gradle.tasks.ProcessJarTask;
import net.minecraftforge.gradle.tasks.ProcessSrcJarTask;
import net.minecraftforge.gradle.tasks.RemapSourcesTask;
import net.minecraftforge.gradle.tasks.abstractutil.ExtractTask;
import net.minecraftforge.gradle.tasks.dev.GenBinaryPatches;
import net.minecraftforge.gradle.tasks.dev.GeneratePatches;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Delete;

import java.io.File;

import static io.github.cruciblemc.forgegradle.DevConstants.*;

public class CrucibleDevPlugin extends DevBasePlugin {
  @Override
  public void applyPlugin() {
    System.setProperty("com.anatawa12.forge.gradle.no-forge-maven-warn", "true");
    System.setProperty("com.anatawa12.forge.gradle.no-maven-central-warn", "true");
    super.applyPlugin();

    // FIXME: Work around unknown version on FG
    project.getDependencies().add(SeparatedLauncher.configurationName,
            "com.anatawa12.forge:separated:1.2-1.1.0");

    // set folders
    getExtension().setFmlDir("forge/fml");
    getExtension().setForgeDir("forge");
    getExtension().setBukkitDir("bukkit");

    createJarProcessTasks();
    createProjectTasks();
    createEclipseTasks();
    createMiscTasks();
    createSourceCopyTasks();
    createPackageTasks();

    // the master setup task.
    Task task = makeTask("setupCrucible", DefaultTask.class);
    task.dependsOn("extractCauldronSources", "generateProjects", "eclipse", "copyAssets");
    task.setGroup("crucible");
    task.setDescription("Configures the development workspace for Crucible.");

    // clean packages
    {
      Delete del = makeTask("cleanPackages", Delete.class);
      del.delete("build/distributions");
      del.setDescription("Cleans up old packages");
    }

    // the master task.
    task = makeTask("buildPackages");
    task.dependsOn("cleanPackages", "packageServer", "packageApi");
    task.setGroup("crucible");
    task.setDescription("Builds all distribution package for Crucible");
  }

  @Override
  protected final DelayedFile getDevJson() {
    return delayedFile(DevConstants.EXTRA_JSON_DEV);
  }

  protected void createJarProcessTasks() {
    ProcessJarTask task2 = makeTask("deobfuscateJar", ProcessJarTask.class);
    {
      task2.setInJar(delayedFile(Constants.JAR_MERGED));
      task2.setOutCleanJar(delayedFile(JAR_SRG_CDN));
      task2.setSrg(delayedFile(JOINED_SRG));
      task2.setExceptorCfg(delayedFile(JOINED_EXC));
      task2.setExceptorJson(delayedFile(EXC_JSON));
      task2.addTransformerClean(delayedFile(FML_RESOURCES + "/fml_at.cfg"));
      task2.addTransformerClean(delayedFile(FORGE_RESOURCES + "/forge_at.cfg"));
      task2.setApplyMarkers(true);
      task2.dependsOn("downloadMcpTools", "mergeJars");
    }

    DeterministicDecompileTask task3 = makeTask("decompile", DeterministicDecompileTask.class);
    {
      task3.setInJar(delayedFile(JAR_SRG_CDN));
      task3.setOutJar(delayedFile(ZIP_DECOMP_CDN));
      task3.setFernFlower(delayedFile(Constants.FERNFLOWER));
      task3.setPatch(delayedFile(MCP_PATCH_DIR));
      task3.setAstyleConfig(delayedFile(ASTYLE_CFG));
      task3.setDoesCache(false);
      task3.dependsOn("downloadMcpTools", "deobfuscateJar");
    }

    ProcessSrcJarTask task4 = makeTask("forgePatchJar", ProcessSrcJarTask.class);
    {
      task4.setInJar(delayedFile(ZIP_DECOMP_CDN));
      task4.setOutJar(delayedFile(ZIP_FORGED_CDN));
      task4.addStage("fml", delayedFile(FML_PATCH_DIR), delayedFile(FML_SOURCES), delayedFile(FML_RESOURCES), delayedFile("{FML_CONF_DIR}/patches/Start.java"), delayedFile(DEOBF_DATA), delayedFile(FML_VERSIONF));
      task4.addStage("forge", delayedFile(FORGE_PATCH_DIR), delayedFile(FORGE_SOURCES), delayedFile(FORGE_RESOURCES));
      task4.addStage("bukkit", null, delayedFile(BUKKIT_SOURCES));
      task4.setDoesCache(false);
      task4.setMaxFuzz(2);
      task4.dependsOn("decompile", "compressDeobfData");
    }

    RemapSourcesTask task6 = makeTask("remapCleanJar", RemapSourcesTask.class);
    {
      task6.setInJar(delayedFile(ZIP_FORGED_CDN));
      task6.setOutJar(delayedFile(REMAPPED_CLEAN));
      task6.setMethodsCsv(delayedFile(METHODS_CSV));
      task6.setFieldsCsv(delayedFile(FIELDS_CSV));
      task6.setParamsCsv(delayedFile(PARAMS_CSV));
      task6.setDoesCache(true);
      task6.setNoJavadocs();
      task6.dependsOn("forgePatchJar");
    }

    task4 = makeTask("cauldronPatchJar", ProcessSrcJarTask.class);
    {
      //task4.setInJar(delayedFile(ZIP_FORGED_CDN)); UNCOMMENT FOR SRG NAMES
      task4.setInJar(delayedFile(REMAPPED_CLEAN));
      task4.setOutJar(delayedFile(ZIP_PATCHED_CDN));
      task4.addStage("Cauldron", delayedFile(EXTRA_PATCH_DIR));
      task4.setDoesCache(false);
      task4.setMaxFuzz(2);
      task4.dependsOn("forgePatchJar", "remapCleanJar");
    }

    task6 = makeTask("remapCauldronJar", RemapSourcesTask.class);
    {
      task6.setInJar(delayedFile(ZIP_PATCHED_CDN));
      task6.setOutJar(delayedFile(ZIP_RENAMED_CDN));
      task6.setMethodsCsv(delayedFile(METHODS_CSV));
      task6.setFieldsCsv(delayedFile(FIELDS_CSV));
      task6.setParamsCsv(delayedFile(PARAMS_CSV));
      task6.setDoesCache(true);
      task6.setNoJavadocs();
      task6.dependsOn("cauldronPatchJar");
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void createSourceCopyTasks() {
    ExtractTask task = makeTask("extractCleanResources", ExtractTask.class);
    {
      task.exclude(JAVA_FILES);
      task.setIncludeEmptyDirs(false);
      task.from(delayedFile(REMAPPED_CLEAN));
      task.into(delayedFile(ECLIPSE_CLEAN_RES));
      task.dependsOn("extractWorkspace", "remapCleanJar");
    }

    task = makeTask("extractCleanSource", ExtractTask.class);
    {
      task.include(JAVA_FILES);
      task.setIncludeEmptyDirs(false);
      task.from(delayedFile(REMAPPED_CLEAN));
      task.into(delayedFile(ECLIPSE_CLEAN_SRC));
      task.dependsOn("extractCleanResources");
    }

    task = makeTask("extractCauldronResources", ExtractTask.class);
    {
      task.exclude(JAVA_FILES);
      task.from(delayedFile(ZIP_RENAMED_CDN));
      task.into(delayedFile(ECLIPSE_CDN_RES));
      task.dependsOn("remapCauldronJar", "extractWorkspace");
      task.onlyIf((Spec) __ -> {
        File dir = delayedFile(ECLIPSE_CDN_RES).call();
        if (!dir.exists())
          return true;

        ConfigurableFileTree tree = project.fileTree(dir);
        tree.include("**/*.java");

        return !tree.isEmpty();
      });
    }

    task = makeTask("extractCauldronSources", ExtractTask.class);
    {
      task.include(JAVA_FILES);
      task.from(delayedFile(ZIP_RENAMED_CDN));
      task.into(delayedFile(ECLIPSE_CDN_SRC));
      task.dependsOn("extractCauldronResources");
      task.onlyIf((Spec) __ -> {
        File dir = delayedFile(ECLIPSE_CDN_SRC).call();
        if (!dir.exists())
          return true;

        ConfigurableFileTree tree = project.fileTree(dir);
        tree.include("**/*.java");

        return !tree.isEmpty();
      });
    }
  }

  private void createProjectTasks() {
    ExtractTask extract = makeTask("extractRes", ExtractTask.class);
    {
      extract.into(delayedFile(EXTRACTED_RES));
      var projectFiles = delayedFile("src/main").call().listFiles();
      if (projectFiles != null) {
        for (File f : projectFiles) {
          if (f.isDirectory())
            continue;
          String path = f.getAbsolutePath();
          if (path.endsWith(".jar") || path.endsWith(".zip"))
            extract.from(delayedFile(path));
        }
      }
    }

    GenDevProjectsTask task = makeTask("generateProjectClean", GenDevProjectsTask.class);
    {
      task.setTargetDir(delayedFile(ECLIPSE_CLEAN));
      task.setJson(delayedFile(EXTRA_JSON_DEV)); // Change to FmlConstants.JSON_BASE eventually, so that it's the base vanilla json
      task.addSource(delayedFile(ECLIPSE_CLEAN_SRC));
      task.addResource(delayedFile(ECLIPSE_CLEAN_RES));

      task.setMcVersion(delayedString("{MC_VERSION}"));
      task.setMappingChannel(delayedString("{MAPPING_CHANNEL}"));
      task.setMappingVersion(delayedString("{MAPPING_VERSION}"));

      task.dependsOn("extractNatives");
    }

    task = makeTask("generateProjectCauldron", GenDevProjectsTask.class);
    {
      task.setJson(delayedFile(EXTRA_JSON_DEV));
      task.setTargetDir(delayedFile(ECLIPSE_CDN));
      task.setUseLibrariesConfiguration(true);

      task.addSource(delayedFile(ECLIPSE_CDN_SRC));
      task.addSource(delayedFile(EXTRA_SOURCES));
      task.addTestSource(delayedFile(EXTRA_TEST_SOURCES));

      task.addResource(delayedFile(ECLIPSE_CDN_RES));
      task.addResource(delayedFile(EXTRA_RESOURCES));
      task.addResource(delayedFile(EXTRACTED_RES));
      task.addTestSource(delayedFile(EXTRA_TEST_SOURCES));

      task.setMcVersion(delayedString("{MC_VERSION}"));
      task.setMappingChannel(delayedString("{MAPPING_CHANNEL}"));
      task.setMappingVersion(delayedString("{MAPPING_VERSION}"));

      task.dependsOn("extractRes", "extractNatives");
    }

    makeTask("generateProjects").dependsOn("generateProjectClean", "generateProjectCauldron");
  }

  private void createEclipseTasks() {
    SubprojectTask task = makeTask("eclipseClean", SubprojectTask.class);
    {
      task.setProjectName(ECLIPSE_CLEAN_PROJECT);
      task.setTasks("eclipse");
      task.dependsOn("extractCleanSource", "generateProjects");
    }
    task = makeTask("eclipseCauldron", SubprojectTask.class);
    {
      task.setProjectName(ECLIPSE_CAULDRON_PROJECT);
      task.setTasks("eclipse");
      task.dependsOn("extractCauldronSources", "generateProjects");
    }

    makeTask("eclipse").dependsOn("eclipseClean", "eclipseCauldron");
  }

  @SuppressWarnings("unused")
  private void createMiscTasks() {
    DelayedFile rangeMapClean = delayedFile("{BUILD_DIR}/tmp/rangemapCLEAN.txt");
    DelayedFile rangeMapDirty = delayedFile("{BUILD_DIR}/tmp/rangemapDIRTY.txt");

    ExtractS2SRangeTask extractRange = makeTask("extractRangeCauldron", ExtractS2SRangeTask.class);
    {
      extractRange.setLibsFromProject(delayedFile(ECLIPSE_CDN + "/build.gradle"), "compile", true);
      extractRange.addIn(delayedFile(ECLIPSE_CDN_SRC));
      extractRange.setRangeMap(rangeMapDirty);
    }

    ApplyS2STask applyS2S = makeTask("retroMapCauldron", ApplyS2STask.class);
    {
      applyS2S.addIn(delayedFile(ECLIPSE_CDN_SRC));
      applyS2S.setOut(delayedFile(PATCH_DIRTY));
      applyS2S.addSrg(delayedFile(MCP_2_SRG_SRG));
      applyS2S.addExc(delayedFile(MCP_EXC));
      applyS2S.addExc(delayedFile(SRG_EXC)); // just in case
      applyS2S.setRangeMap(rangeMapDirty);
      applyS2S.dependsOn("genSrgs", extractRange);
      String[] paths = {DevConstants.FML_RESOURCES, DevConstants.FORGE_RESOURCES, DevConstants.EXTRA_RESOURCES};
      for (String path : paths) {
        for (File f : project.fileTree(delayedFile(path).call()).getFiles()) {
          if (f.getPath().endsWith(".exc"))
            applyS2S.addExc(f);
          else if (f.getPath().endsWith(".srg"))
            applyS2S.addSrg(f);
        }
      }
    }

    extractRange = makeTask("extractRangeClean", ExtractS2SRangeTask.class);
    {
      extractRange.setLibsFromProject(delayedFile(ECLIPSE_CLEAN + "/build.gradle"), "compile", true);
      extractRange.addIn(delayedFile(REMAPPED_CLEAN));
      extractRange.setRangeMap(rangeMapClean);
    }

    applyS2S = makeTask("retroMapClean", ApplyS2STask.class);
    {
      applyS2S.addIn(delayedFile(REMAPPED_CLEAN));
      applyS2S.setOut(delayedFile(PATCH_CLEAN));
      applyS2S.addSrg(delayedFile(MCP_2_SRG_SRG));
      applyS2S.addExc(delayedFile(MCP_EXC));
      applyS2S.addExc(delayedFile(SRG_EXC)); // just in case
      applyS2S.setRangeMap(rangeMapClean);
      applyS2S.dependsOn("genSrgs", extractRange);
    }

    GeneratePatches task2 = makeTask("genPatches", GeneratePatches.class);
    {
      task2.setPatchDir(delayedFile(EXTRA_PATCH_DIR));
      task2.setOriginal(delayedFile(ECLIPSE_CLEAN_SRC));
      task2.setChanged(delayedFile(ECLIPSE_CDN_SRC));
      task2.setOriginalPrefix("../src-base/minecraft");
      task2.setChangedPrefix("../src-work/minecraft");
      task2.getTaskDependencies().getDependencies(task2).clear(); // remove all the old dependants.
      task2.setGroup("Crucible");
      task2.setDescription("Create patches from your changes within the dev workspace");
    }

    Delete clean = makeTask("cleanCauldron", Delete.class);
    {
      clean.delete("eclipse");
      clean.setGroup("Clean");
    }
    project.getTasks().getByName("clean").dependsOn("cleanCauldron");

    ObfuscateTask obf = makeTask("obfuscateJar", ObfuscateTask.class);
    {
      obf.setSrg(delayedFile(MCP_2_NOTCH_SRG));
      obf.setExc(delayedFile(JOINED_EXC));
      obf.setReverse(false);
      obf.setPreFFJar(delayedFile(JAR_SRG_CDN));
      obf.setOutJar(delayedFile(REOBF_TMP));
      obf.setProjectName(ECLIPSE_CAULDRON_PROJECT);
      obf.setMethodsCsv(delayedFile(METHODS_CSV));
      obf.setFieldsCsv(delayedFile(FIELDS_CSV));
      obf.dependsOn("genSrgs", ECLIPSE_CAULDRON_PROJECT + ":jar");
    }

    GenBinaryPatches task3 = makeTask("genBinPatches", GenBinaryPatches.class);
    {
      task3.setCleanClient(delayedFile(Constants.JAR_CLIENT_FRESH));
      task3.setCleanServer(delayedFile(Constants.JAR_SERVER_FRESH));
      task3.setCleanMerged(delayedFile(Constants.JAR_MERGED));
      task3.setDirtyJar(delayedFile(REOBF_TMP));
      task3.setDeobfDataLzma(delayedFile(DEOBF_DATA));
      task3.setOutJar(delayedFile(BINPATCH_TMP));
      task3.setSrg(delayedFile(JOINED_SRG));
      task3.addPatchList(delayedFileTree(EXTRA_PATCH_DIR));
      task3.addPatchList(delayedFileTree(FORGE_PATCH_DIR));
      task3.addPatchList(delayedFileTree(FML_PATCH_DIR));
      task3.dependsOn("obfuscateJar", "compressDeobfData");
    }

    var uncode = makeTask("uncodeCrucible", UncodeJarTask.class);
    {
      uncode.setDescription("Removes all code from minecraft classes");
      uncode.setInputJar(delayedFile(ECLIPSE_CDN + "/build/libs/cauldron.jar"));
      uncode.setOutJar(delayedFile("build/tmp/crucibleUncoded.jar"));
      uncode.dependsOn(ECLIPSE_CAULDRON_PROJECT + ":jar");
    }
  }

  private void createPackageTasks() {

    final DelayedJar uni = makeTask("packageServer", DelayedJar.class);
    {
      uni.getArchiveClassifier().set("server");
      uni.getInputs().file(delayedFile(EXTRA_JSON_REL));
      uni.getOutputs().upToDateWhen(CALL_FALSE);
      uni.from(delayedZipTree(BINPATCH_TMP));
      uni.from(delayedFileTree(EXTRA_RESOURCES));
      uni.from(delayedFileTree(FORGE_RESOURCES));
      uni.from(delayedFileTree(FML_RESOURCES));
      uni.from(delayedFileTree(EXTRACTED_RES));
      uni.from(delayedFile(FML_VERSIONF));
      uni.from(delayedFile(FML_LICENSE));
      uni.from(delayedFile(FML_CREDITS));
      uni.from(delayedFile(FORGE_LICENSE));
      uni.from(delayedFile(FORGE_CREDITS));
      uni.from(delayedFile(PAULSCODE_LISCENCE1));
      uni.from(delayedFile(PAULSCODE_LISCENCE2));
      uni.from(delayedFile(DEOBF_DATA));
      uni.from(delayedFile(CHANGELOG));
      uni.from(delayedFile(VERSION_JSON));
      uni.exclude("devbinpatches.pack.lzma");
      uni.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
      uni.setIncludeEmptyDirs(false);

      uni.getDestinationDirectory().set(delayedFile("{BUILD_DIR}/distributions").call());
      uni.dependsOn("genBinPatches");
    }
    project.getArtifacts().add("archives", uni);

    final DelayedJar lib = makeTask("packageApi", DelayedJar.class);
    {
      lib.from(delayedZipTree("build/tmp/crucibleUncoded.jar"));
      lib.getDestinationDirectory().set(delayedFile("{BUILD_DIR}/libs").call());
      lib.setIncludeEmptyDirs(false);
      lib.dependsOn("uncodeCrucible");
    }
    project.getArtifacts().add("archives", lib);
  }

  @Override
  public void afterEvaluate() {
    super.afterEvaluate();

    SubprojectTask task = (SubprojectTask) project.getTasks().getByName("eclipseClean");
    task.configureProject(getExtension().getSubprojects());
    task.configureProject(getExtension().getCleanProject());

    task = (SubprojectTask) project.getTasks().getByName("eclipseCauldron");
    task.configureProject(getExtension().getSubprojects());
    task.configureProject(getExtension().getCleanProject());
  }
}
