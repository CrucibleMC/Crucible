package thermos;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ThermosClassTransformer implements IClassTransformer {
    private static final String TARGET_CLASS =
            "climateControl/utils/ChunkGeneratorExtractor";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!TARGET_CLASS.equals(transformedName.replace('.', '/'))) {
            return basicClass;
        }

        if (isUndergroundBiomesPresent()) {
            return basicClass;
        }

        FMLLog.log(Level.INFO,
                "Thermos: Patching ChunkGeneratorExtractor for Climate Control compatibility");

        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(basicClass);
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("extractFrom")
                    && mn.desc.equals("(Lnet/minecraft/world/WorldServer;)Lnet/minecraft/world/chunk/IChunkProvider;")) {

                patchMethod(mn);
                break;
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private void patchMethod(MethodNode mn) {
        mn.instructions.clear();

        boolean obf = isObfuscated();

        String worldClass   = obf ? "ahb" : "net/minecraft/world/World";
        String fieldName    = obf ? "v"   : "chunkProvider";
        String fieldDesc    = obf ? "Lapu;" :
                "Lnet/minecraft/world/chunk/IChunkProvider;";

        InsnList insns = new InsnList();
        insns.add(new VarInsnNode(ALOAD, 1));
        insns.add(new FieldInsnNode(GETFIELD, worldClass, fieldName, fieldDesc));
        insns.add(new InsnNode(ARETURN));

        mn.instructions.add(insns);
    }

    private boolean isUndergroundBiomesPresent() {
        try {
            Class.forName("exterminatorJeff.undergroundBiomes.worldGen.ChunkProviderWrapper");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isObfuscated() {
        try {
            Class.forName("net.minecraft.world.World");
            return false;
        } catch (Throwable t) {
            return true;
        }
    }
}
