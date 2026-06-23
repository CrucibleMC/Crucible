package io.github.crucible.patches;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class StreamsTransformer implements IClassTransformer {
    private static final String TARGET_CLASS =
            "streams.world.gen.structure.RiverComponent$";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals(TARGET_CLASS)) {
            return basicClass;
        }

        System.out.println("[Crucible] Found " + TARGET_CLASS + ", patching…");

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            if ("<init>".equals(method.name) && "()V".equals(method.desc)) {
                patchConstructor(method);
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private void patchConstructor(MethodNode method) {
        InsnList insns = method.instructions;

        for (Iterator<AbstractInsnNode> it = insns.iterator(); it.hasNext(); ) {
            AbstractInsnNode insn = it.next();

            if (insn.getOpcode() == Opcodes.ICONST_2 &&
                    insn.getNext() instanceof FieldInsnNode) {

                FieldInsnNode field = (FieldInsnNode) insn.getNext();

                if (field.name.contains("MinSourceBackWallHeight")) {
                    insns.set(insn, new InsnNode(Opcodes.ICONST_0));
                    System.out.println("[Crucible] Patched MinSourceBackWallHeight");
                    return;
                }
            }
        }

        System.out.println("[Crucible] Failed to find MinSourceBackWallHeight");
    }
}
