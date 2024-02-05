package io.github.cruciblemc.forgegradle;

import com.google.common.base.Throwables;
import groovy.lang.Closure;
import io.github.cruciblemc.forgegradle.tasks.dev.ObfuscateTask;
import net.minecraftforge.gradle.common.BasePlugin;
import net.minecraftforge.gradle.common.Constants;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.json.JsonFactory;
import net.minecraftforge.gradle.json.version.AssetIndex;
import net.minecraftforge.gradle.json.version.Library;
import net.minecraftforge.gradle.tasks.CopyAssetsTask;
import net.minecraftforge.gradle.tasks.GenSrgTask;
import net.minecraftforge.gradle.tasks.MergeJarsTask;
import net.minecraftforge.gradle.tasks.abstractutil.DownloadTask;
import net.minecraftforge.gradle.tasks.abstractutil.ExtractTask;
import net.minecraftforge.gradle.tasks.dev.CompressLZMA;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;

import java.io.File;

public class DevBasePlugin extends BasePlugin<DevExtension> {
  protected static final String[] JAVA_FILES = new String[]{"**.java", "*.java", "**/*.java"};

  @Override
  public void applyPlugin() {
    ExtractTask extractWorkspace = makeTask("extractWorkspace", ExtractTask.class);
    {
      extractWorkspace.getOutputs().upToDateWhen(new Closure<Boolean>(null) {
        public Boolean call(Object... obj) {
          File file = new File(project.getProjectDir(), "eclipse");
          return (file.exists() && file.isDirectory());
        }
      });
      extractWorkspace.from(delayedFile(DevConstants.WORKSPACE_ZIP));
      extractWorkspace.into(delayedFile(DevConstants.WORKSPACE));
    }

    CompressLZMA compressDeobfData = makeTask("compressDeobfData", CompressLZMA.class);
    {
      compressDeobfData.setInputFile(delayedFile(DevConstants.NOTCH_2_SRG_SRG));
      compressDeobfData.setOutputFile(delayedFile(DevConstants.DEOBF_DATA));
      compressDeobfData.dependsOn("genSrgs");
    }

    MergeJarsTask mergeJars = makeTask("mergeJars", MergeJarsTask.class);
    {
      mergeJars.setClient(delayedFile(Constants.JAR_CLIENT_FRESH));
      mergeJars.setServer(delayedFile(Constants.JAR_SERVER_FRESH));
      mergeJars.setOutJar(delayedFile(Constants.JAR_MERGED));
      mergeJars.setMergeCfg(delayedFile(DevConstants.MERGE_CFG));
      mergeJars.setMcVersion(delayedString("{MC_VERSION}"));
      mergeJars.dependsOn("downloadClient", "downloadServer");
    }

    CopyAssetsTask copyAssets = makeTask("copyAssets", CopyAssetsTask.class);
    {
      copyAssets.setAssetsDir(delayedFile(Constants.ASSETS));
      copyAssets.setOutputDir(delayedFile(DevConstants.ECLIPSE_ASSETS));
      copyAssets.setAssetIndex(assetIndexClosure());
      copyAssets.dependsOn("getAssets", "extractWorkspace");
    }

    GenSrgTask genSrgs = makeTask("genSrgs", GenSrgTask.class);
    {
      genSrgs.setInSrg(delayedFile(DevConstants.JOINED_SRG));
      genSrgs.setInExc(delayedFile(DevConstants.JOINED_EXC));
      genSrgs.setMethodsCsv(delayedFile(DevConstants.METHODS_CSV));
      genSrgs.setFieldsCsv(delayedFile(DevConstants.FIELDS_CSV));
      genSrgs.setNotchToSrg(delayedFile(DevConstants.NOTCH_2_SRG_SRG));
      genSrgs.setNotchToMcp(delayedFile(DevConstants.NOTCH_2_MCP_SRG));
      genSrgs.setSrgToMcp(delayedFile(DevConstants.SRG_2_MCP_SRG));
      genSrgs.setMcpToSrg(delayedFile(DevConstants.MCP_2_SRG_SRG));
      genSrgs.setMcpToNotch(delayedFile(DevConstants.MCP_2_NOTCH_SRG));
      genSrgs.setSrgExc(delayedFile(DevConstants.SRG_EXC));
      genSrgs.setMcpExc(delayedFile(DevConstants.MCP_EXC));
      genSrgs.dependsOn("extractMcpData");
    }
  }

  @Override
  public final void applyOverlayPlugin() {
    // nothing.
  }

  @Override
  public final boolean canOverlayPlugin() {
    return false;
  }

  @Override
  protected DelayedFile getDevJson() {
    return delayedFile(DevConstants.JSON_DEV);
  }

  @Override
  public void afterEvaluate() {
    super.afterEvaluate();

    // set obfuscate extras
    ObfuscateTask obfuscateJar = (ObfuscateTask) project.getTasks().getByName("obfuscateJar");
    obfuscateJar.setExtraSrg(getExtension().getSrgExtra());
    obfuscateJar.configureProject(getExtension().getSubprojects());
    obfuscateJar.configureProject(getExtension().getDirtyProject());

    project.getTasks().getByName("getAssetsIndex").dependsOn("getVersionJson");

    ExtractTask extractNatives = makeTask("extractNativesNew", ExtractTask.class);
    {
      extractNatives.exclude("META-INF", "META-INF/**", "META-INF/*");
      extractNatives.into(delayedFile(Constants.NATIVES_DIR));
    }

    Copy copyNatives = makeTask("extractNatives", Copy.class);
    {
      copyNatives.from(delayedFile(Constants.NATIVES_DIR));
      copyNatives.exclude("META-INF", "META-INF/**", "META-INF/*");
      copyNatives.into(delayedFile(DevConstants.ECLIPSE_NATIVES));
      copyNatives.dependsOn("extractWorkspace", extractNatives);
    }

    DelayedFile devJson = getDevJson();
    if (devJson == null) {
      project.getLogger().info("Dev json not set, could not create native downloads tasks");
      return;
    }

    if (version == null) {
      File jsonFile = devJson.call().getAbsoluteFile();
      try {
        version = JsonFactory.loadVersion(jsonFile, jsonFile.getParentFile());
      } catch (Exception e) {
        project.getLogger().error(jsonFile + " could not be parsed");
        Throwables.throwIfUnchecked(e);
      }
    }

    for (Library lib : version.getLibraries()) {
      if (lib.natives != null) {
        String path = lib.getPathNatives();
        String taskName = "downloadNatives-" + lib.getArtifactName().split(":")[1];

        DownloadTask task = makeTask(taskName, DownloadTask.class);
        {
          task.setOutput(delayedFile("{CACHE_DIR}/minecraft/" + path));
          task.setUrl(delayedString(lib.getUrl() + path));
        }

        extractNatives.from(delayedFile("{CACHE_DIR}/minecraft/" + path));
        extractNatives.dependsOn(taskName);
      }
    }
  }

  protected Class<DevExtension> getExtensionClass() {
    return DevExtension.class;
  }

  protected DevExtension getOverlayExtension() {
    // never happens.
    return null;
  }

  @Override
  public String resolve(String pattern, Project project, DevExtension exten) {
    pattern = super.resolve(pattern, project, exten);

    // MCP_DATA_DIR wont be resolved if the data dir doesnt eixts,,, hence...
    pattern = pattern.replace("{MCP_DATA_DIR}", "{FML_CONF_DIR}");

    // For simplicities sake, if the version is in the standard format of {MC_VERSION}-{realVersion}
    // lets trim the MC version from the replacement string.
    String version = project.getVersion().toString();
    String mcSafe = exten.getVersion().replace('-', '_');
    if (version.startsWith(mcSafe + "-")) {
      version = version.substring(mcSafe.length() + 1);
    }
    pattern = pattern.replace("{VERSION}", version);
    pattern = pattern.replace("{MAIN_CLASS}", exten.getMainClass());
    pattern = pattern.replace("{FML_TWEAK_CLASS}", exten.getTweakClass());
    pattern = pattern.replace("{INSTALLER_VERSION}", exten.getInstallerVersion());
    pattern = pattern.replace("{FML_DIR}", exten.getFmlDir());
    pattern = pattern.replace("{FORGE_DIR}", exten.getForgeDir());
    pattern = pattern.replace("{BUKKIT_DIR}", exten.getBukkitDir());
    pattern = pattern.replace("{FML_CONF_DIR}", exten.getFmlDir() + "/conf");
    return pattern;
  }

  public Closure<AssetIndex> assetIndexClosure() {
    return new Closure<>(this, null) {
      public AssetIndex call(Object... obj) {
        return getAssetIndex();
      }
    };
  }
}
