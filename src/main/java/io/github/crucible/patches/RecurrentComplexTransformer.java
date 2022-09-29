package io.github.crucible.patches;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import pw.prok.imagine.asm.ImagineASM;
import pw.prok.imagine.asm.Transformer;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

@Transformer.RegisterTransformer
public class RecurrentComplexTransformer implements Transformer {
    @Override
    public void transform(ImagineASM asm) {
        if (asm.is("ivorius.reccomplex.structures.generic.matchers.BiomeMatcher")) {
            System.out.println("[Crucible] Found ivorius.reccomplex.structures.generic.matchers.BiomeMatcher, trying to patch it!");
            InsnList instructions = asm.method("ofTypes", "([Lnet/minecraftforge/common/BiomeDictionary$Type;)Ljava/lang/String;").instructions();

            AbstractInsnNode abstractInsnNode = instructions.getFirst();
            boolean appliedPatch = false;
            while (abstractInsnNode != null) {
                if (abstractInsnNode.getOpcode() == INVOKESTATIC) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
                    if ("joptsimple/internal/Strings".equals(methodInsnNode.owner) &&
                            "join".equals(methodInsnNode.name) &&
                            "(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;"
                                    .equals(methodInsnNode.desc)) {
                        methodInsnNode.owner = "io/github/crucible/patches/AsmHooks";
                        appliedPatch = true;
                        System.out.println("[Crucible] Patched joptsimple.internal.Strings#join() call!");
                    }
                }
                abstractInsnNode = abstractInsnNode.getNext();
            }
            if (!appliedPatch) {
                System.out.println("[Crucible] RecurrentComplexTransformer: " +
                        "unable to find joptsimple.internal.Strings#join(), skipping it!");
            }
        }
    }
}
