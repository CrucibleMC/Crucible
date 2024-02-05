package io.github.cruciblemc.forgegradle.tasks.dev;

import com.google.common.io.Files;
import groovy.lang.Closure;
import io.github.cruciblemc.forgegradle.DevExtension;
import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.delayed.DelayedString;
import net.minecraftforge.gradle.json.JsonFactory;
import net.minecraftforge.gradle.json.version.Library;
import net.minecraftforge.gradle.json.version.Version;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraftforge.gradle.common.Constants.EXT_NAME_MC;
import static net.minecraftforge.gradle.common.Constants.NEWLINE;

public class GenDevProjectsTask extends DefaultTask {
  private static final String TEMPLATE = """
          apply plugin: 'java-library'
          apply plugin: 'eclipse'
                  
          repositories {
            mavenCentral()
          %s
          }
                      
          dependencies {
          %s
          }
                      
          jar {
            exclude 'GradleStart*', 'net/minecraftforge/gradle/**'
          }
                      
          // source set definition
          %s
                    
          processResources {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
          }
                     
          def links = []
          def dupes = []
          eclipse.project.file.withXml { provider ->
            def node = provider.asNode()
            links = []
            dupes = []
            node.linkedResources.link.each { child ->
              def path = child.location.text()
              if (path in dupes) {
                child.replaceNode {}
              } else {
                dupes.add(path)
                def newName = path.split('/')[-2..-1].join('/')
                links += newName
                child.replaceNode {
                  link{
                    name(newName)
                    type('2')
                    location(path)
                  }
                }
              }
            }
          }
                      
          eclipse.classpath.file.withXml {
            def node = it.asNode()
            node.classpathentry.each { child ->
              if (child.@kind == 'src' && !child.@path.contains('/')) child.replaceNode {}
              if (child.@path in links) links.remove(child.@path)
            }
            links.each { link -> node.appendNode('classpathentry', [kind:'src', path:link]) }
          }
          tasks.eclipseClasspath.dependsOn 'eclipseProject' //Make them run in correct order"
          """;
  @Internal
  protected DelayedFile targetDir;

  @InputFile
  protected DelayedFile json;

  @Input
  @Optional
  protected String[] repos = new String[0];

  @Input
  protected boolean useLibrariesConfiguration = false;

  @Input
  @Optional
  private DelayedString mappingChannel, mappingVersion, mcVersion;

  private final List<DelayedFile> sources = new ArrayList<>();
  private final List<DelayedFile> resources = new ArrayList<>();
  private final List<DelayedFile> testSources = new ArrayList<>();
  private final List<DelayedFile> testResources = new ArrayList<>();

  private final ArrayList<String> deps = new ArrayList<>();

  public GenDevProjectsTask() {
    this.getOutputs().file(getTargetFile());
  }

  @TaskAction
  public void doTask() throws IOException {
    parseJson();
    writeFile();
  }

  private void parseJson() throws IOException {
    Version version = JsonFactory.loadVersion(getJson(), getJson().getParentFile());

    for (Library lib : version.getLibraries()) {
      if (lib.name.contains("fixed") || lib.natives != null || lib.extract != null) {
        continue;
      } else {
        deps.add(lib.getArtifactName());
      }
    }
  }

  private void writeFile() throws IOException {
    File file = getProject().file(getTargetFile().call());
    file.getParentFile().mkdirs();
    Files.touch(file);

    StringBuilder o = new StringBuilder();

    for (String repo : getRepos()) {
      a(o, "  maven {",
              " url = '" + repo + "'",
              "  }"
      );
    }
    for (String repo : ((DevExtension) getProject().getExtensions().getByName(EXT_NAME_MC)).getRepos()) {
      a(o, "  maven {",
              "    url = '" + repo + "'",
              "  }"
      );
    }
    var repos = o.toString();
    o.setLength(0);

    if (useLibrariesConfiguration) {
      var libraryOverwrite = getProject().getConfigurations().getByName("libraries").getDependencies()
              .stream().map(dep -> dep.getGroup() + ':' + dep.getName() + ':' + dep.getVersion())
              .collect(Collectors.toList());

      o.append(NEWLINE).append("  // Library overwrites").append(NEWLINE);
      for (String dep : libraryOverwrite) {
        o.append("  api '").append(dep).append('\'').append(NEWLINE);
      }

      o.append("  // MC libraries").append(NEWLINE);
      for (String dep : deps) {
        o.append("  api '").append(dep).append('\'').append(NEWLINE);
      }
    } else {
      for (String dep : deps) {
        o.append("  api '").append(dep).append('\'').append(NEWLINE);
      }
    }

    String channel = getMappingChannel();
    String version = getMappingVersion();
    String mcversion = getMcVersion();
    if (version != null && channel != null) {
      o.append("  api group: 'de.oceanlabs.mcp', name:'mcp_").append(channel).append("', version:'").append(version).append('-').append(mcversion).append("', ext:'zip'");
    }
    o.append("  testImplementation 'junit:junit:4.5'");
    var deps = o.toString();
    o.setLength(0);

    URI base = targetDir.call().toURI();

    if (resources.size() > 0 || sources.size() > 0 || testSources.size() > 0 || testResources.size() > 0) {
      a(o, "sourceSets {");
      a(o, "  main {");
      if (sources.size() > 0) {
        a(o, "    java {");
        for (DelayedFile src : sources) {
          o.append("      srcDir '").append(relative(base, src)).append('\'').append(NEWLINE);
        }
        a(o, "    }");
      }
      if (resources.size() > 0) {
        a(o, "    resources {");
        for (DelayedFile src : resources) {
          o.append("      srcDir '").append(relative(base, src)).append('\'').append(NEWLINE);
        }
        a(o, "    }");
      }
      a(o, "  }");
      a(o, "  test {");
      if (testSources.size() > 0) {
        a(o, "    java {");

        for (DelayedFile src : testSources) {
          o.append("      srcDir '").append(relative(base, src)).append('\'').append(NEWLINE);
        }
        a(o, "    }");
      }
      if (testResources.size() > 0) {
        a(o, "    resources {");
        for (DelayedFile src : testResources) {
          o.append("      srcDir '").append(relative(base, src)).append('\'').append(NEWLINE);
        }
        a(o, "    }");
      }
      a(o, "  }");
      a(o, "}");
    }
    var srcSet = o.toString();
    o.setLength(0);

    String buildFile = String.format(TEMPLATE, repos, deps, srcSet);
    Files.asCharSink(file, Charset.defaultCharset()).write(buildFile);
  }

  private String relative(URI base, DelayedFile src) {
    String relative = base.relativize(src.call().toURI()).getPath().replace('\\', '/');
    if (!relative.endsWith("/")) relative += "/";
    return relative;
  }

  private void a(StringBuilder out, String... lines) {
    for (String line : lines) {
      out.append(line).append(NEWLINE);
    }
  }

  private Closure<File> getTargetFile() {
    return new Closure<File>(this) {
      private static final long serialVersionUID = -6333350974905684295L;

      @Override
      public File call() {
        return new File(getTargetDir(), "build.gradle");
      }

      @Override
      public File call(Object obj) {
        return new File(getTargetDir(), "build.gradle");
      }
    };
  }

  public File getTargetDir() {
    return targetDir.call();
  }

  public void setTargetDir(DelayedFile targetDir) {
    this.targetDir = targetDir;
  }

  public GenDevProjectsTask addSource(DelayedFile source) {
    sources.add(source);
    return this;
  }

  public GenDevProjectsTask addResource(DelayedFile resource) {
    resources.add(resource);
    return this;
  }

  public GenDevProjectsTask addTestSource(DelayedFile source) {
    testSources.add(source);
    return this;
  }

  public GenDevProjectsTask addTestResource(DelayedFile resource) {
    testResources.add(resource);
    return this;
  }

  public File getJson() {
    return json.call();
  }

  public void setJson(DelayedFile json) {
    this.json = json;
  }

  public String getMappingChannel() {
    String channel = mappingChannel.call();
    return channel.equals("{MAPPING_CHANNEL}") ? null : channel;
  }

  public void setMappingChannel(DelayedString mChannel) {
    this.mappingChannel = mChannel;
  }

  public String getMappingVersion() {
    String version = mappingVersion.call();
    return version.equals("{MAPPING_VERSION}") ? null : version;
  }

  public void setMappingVersion(DelayedString mappingVersion) {
    this.mappingVersion = mappingVersion;
  }

  public String getMcVersion() {
    return mcVersion.call();
  }

  public void setMcVersion(DelayedString mcVersion) {
    this.mcVersion = mcVersion;
  }

  public void setRepos(String[] repos) {
    this.repos = repos;
  }

  public String[] getRepos() {
    return repos;
  }

  public boolean isUseLibrariesConfiguration() {
    return useLibrariesConfiguration;
  }

  public void setUseLibrariesConfiguration(boolean useLibrariesConfiguration) {
    this.useLibrariesConfiguration = useLibrariesConfiguration;
  }
}
