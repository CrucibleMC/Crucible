package io.github.crucible.patches;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import pw.prok.imagine.asm.ImagineASM;
import pw.prok.imagine.asm.Transformer;

import java.util.Iterator;

@Transformer.RegisterTransformer
public class StreamsTransformer implements Transformer {
    @Override
    public void transform(ImagineASM asm) {
        if (asm.is("streams.world.gen.structure.RiverComponent$")) {
            System.out.println("[Crucible] Found streams.world.gen.structure.RiverComponent$, trying to patch it!");
            InsnList instructions = asm.method("<init>", "()V").instructions(); //We just need to replace a number
            AbstractInsnNode toReplace = null;
            Iterator<AbstractInsnNode> i = instructions.iterator();
            while (i.hasNext()) {
                AbstractInsnNode ins = i.next();
                if (ins.getOpcode() == Opcodes.ICONST_2) {
                    if (ins.getNext() instanceof FieldInsnNode) {
                        FieldInsnNode fieldAccess = (FieldInsnNode) ins.getNext();
                        if (fieldAccess.name.contains("MinSourceBackWallHeight")) {
                            toReplace = fieldAccess.getPrevious();
                            break;
                        }
                    }
                }
            }
            if (toReplace == null) {
                System.out.println("[Crucible] Unable to find MinSourceBackWallHeight, skipping patch!");
            } else {
                instructions.set(toReplace, new InsnNode(Opcodes.ICONST_0));
                System.out.println("[Crucible] Patched MinSourceBackWallHeight's previous opcode!");
            }
        }
    }
}
