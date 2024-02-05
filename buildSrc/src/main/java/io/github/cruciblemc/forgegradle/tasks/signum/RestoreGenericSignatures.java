package io.github.cruciblemc.forgegradle.tasks.signum;

import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.tasks.abstractutil.CachedTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class RestoreGenericSignatures extends CachedTask {
  private static final Map<String, String> SIGNATURE_MAP = new HashMap<>();

  @InputFile
  private DelayedFile inJar;

  @Cached
  @OutputFile
  public DelayedFile outJar;

  public RestoreGenericSignatures() {
    this.getInputs().property("signatureMap", SIGNATURE_MAP);
  }

  @TaskAction
  public void restore() throws Throwable {
    File input = this.inJar.call().getCanonicalFile();

    if (!input.exists())
      throw new FileNotFoundException("Could not locate file " + input);

    ClassCollection collection = JarUtils.readFromJar(input);

    for (ClassNode node : collection.getClasses()) {
      if (node.fields != null) {
        for (FieldNode field : node.fields) {
          String signature = SIGNATURE_MAP.get(field.name);

          if (signature != null) {
            field.signature = signature;
          }
        }
      }

      if (node.methods != null) {
        for (MethodNode method : node.methods) {
          String signature = SIGNATURE_MAP.get(method.name);

          if (signature != null) {
            method.signature = signature;
          }
        }
      }
    }

    File output = this.outJar.call().getCanonicalFile();

    if (output.exists()) {
      output.delete();
    }

    JarUtils.writeToJar(collection, output);
  }

  public File getInJar() {
    return this.inJar.call();
  }

  public void setInJar(DelayedFile inJar) {
    this.inJar = inJar;
  }

  public File getOutJar() {
    return this.outJar.call();
  }

  public void setOutJar(DelayedFile outJar) {
    this.outJar = outJar;
  }

  public static Map<String, String> getSignatureMap() {
    return SIGNATURE_MAP;
  }

  static {
    // buttonList
    SIGNATURE_MAP.put("field_146292_n", "Ljava/util/List<Lnet/minecraft/client/gui/GuiButton;>;");
    // labelList
    SIGNATURE_MAP.put("field_146293_o", "Ljava/util/List<Lnet/minecraft/client/gui/GuiLabel;>;");
    // playerEntityList
    SIGNATURE_MAP.put("field_72404_b", "Ljava/util/List<Lnet/minecraft/entity/player/EntityPlayerMP;>;");
    // stringToClassMapping
    SIGNATURE_MAP.put("field_75625_b", "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<+Lnet/minecraft/entity/Entity;>;>;");
    // classToStringMapping
    SIGNATURE_MAP.put("field_75626_c", "Ljava/util/Map<Ljava/lang/Class<+Lnet/minecraft/entity/Entity;>;Ljava/lang/String;>;");
    // IDtoClassMapping
    SIGNATURE_MAP.put("field_75623_d", "Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Class<+Lnet/minecraft/entity/Entity;>;>;");
    // classToIDMapping
    SIGNATURE_MAP.put("field_75624_e", "Ljava/util/Map<Ljava/lang/Class<+Lnet/minecraft/entity/Entity;>;Ljava/lang/Integer;>;");
    // stringToIDMapping
    SIGNATURE_MAP.put("field_75622_f", "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;");
    // entityEggs
    SIGNATURE_MAP.put("field_75627_a", "Ljava/util/HashMap<Ljava/lang/Integer;Lnet/minecraft/entity/EntityList$EntityEggInfo;>;");

    // addInformation
    SIGNATURE_MAP.put("func_77624_a", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Ljava/util/List<Ljava/lang/String;>;Z)V");
  }

}
