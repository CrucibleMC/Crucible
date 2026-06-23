package io.github.crucible.patches;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class RecurrentComplexTransformer implements IClassTransformer {
    private static final String TARGET_CLASS =
            "ivorius.reccomplex.structures.generic.matchers.BiomeMatcher";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals(TARGET_CLASS)) {
            return basicClass;
        }

        FMLLog.info("[Crucible] Found %s, attempting ASM patch", TARGET_CLASS);

        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);

        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("ofTypes")) continue;
            if (!method.desc.equals("([Lnet/minecraftforge/common/BiomeDictionary$Type;)Ljava/lang/String;"))
                continue;

            InsnList insns = method.instructions;

            for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == INVOKESTATIC && insn instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode) insn;

                    if (m.owner.equals("joptsimple/internal/Strings")
                            && m.name.equals("join")
                            && m.desc.equals("(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;")) {

                        m.owner = "io/github/crucible/patches/AsmHooks";
                        patched = true;

                        FMLLog.info("[Crucible] Patched Strings.join() call in BiomeMatcher");
                        break;
                    }
                }
            }
        }

        if (!patched) {
            FMLLog.warning("[Crucible] Failed to patch BiomeMatcher: join() call not found");
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
