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
        //Patching scala gives me headaches
        if (asm.is("streams.world.gen.structure.RiverComponent$")) {
            System.out.println("[Crucible] found streams.world.gen.structure.RiverComponent$");
            InsnList instructions = asm.method("<init>", "()V").instructions(); //We just need to replace a number
            AbstractInsnNode toReplace = null;
            Iterator<AbstractInsnNode> i = instructions.iterator();
            while (i.hasNext()) {
                AbstractInsnNode ins = i.next();
                if (ins.getOpcode() == Opcodes.ICONST_2) {
                    //System.out.println("[Crucible] found Opcodes.ICONST_2, checking next instruction");
                    if (ins.getNext() instanceof FieldInsnNode) {
                        FieldInsnNode fieldAccess = (FieldInsnNode) ins.getNext();
                        //System.out.printf("Field access desc:%s name:%s owner:%s%n", fieldAccess.desc, fieldAccess.name, fieldAccess.owner);
                        if (fieldAccess.name.contains("MinSourceBackWallHeight")) {
                            System.out.println("[Crucible] found MinSourceBackWallHeight!");
                            toReplace = fieldAccess.getPrevious();
                            break;
                        }
                    }
                }
            }
            if (toReplace == null) {
                System.out.println("[Crucible] unable to find MinSourceBackWallHeight, ignoring it!");
            } else {
                System.out.println("[Crucible] replacing MinSourceBackWallHeight's previous opcode!");
                instructions.set(toReplace, new InsnNode(Opcodes.ICONST_0));
            }
        }
    }
}
