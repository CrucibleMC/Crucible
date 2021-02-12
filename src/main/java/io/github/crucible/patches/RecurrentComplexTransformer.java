package io.github.crucible.patches;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import pw.prok.imagine.asm.ImagineASM;
import pw.prok.imagine.asm.Transformer;

import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Transformer.RegisterTransformer
public class RecurrentComplexTransformer implements Transformer {
    @Override
    public void transform(ImagineASM asm) {
        if (asm.is("ivorius.reccomplex.structures.generic.matchers.BiomeMatcher")) {
            InsnList instructions = asm.method("ofTypes", "([Lnet/minecraftforge/common/BiomeDictionary$Type;)Ljava/lang/String;").instructions();
            instructions.set(instructions.get(12),
                    new MethodInsnNode(INVOKESTATIC, "io/github/crucible/patches/Hook","join","(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;", false));

//            InsnList toAdd = new InsnList();
//            toAdd.add(new MethodInsnNode(INVOKESTATIC, "io/github/crucible/patches/Hook","join","(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;", false));
//            toAdd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
//            toAdd.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
//            toAdd.add(new InsnNode(ARETURN));
//            instructions.insertBefore(new MethodInsnNode(INVOKESTATIC, "joptsimple/internal/Strings","join","(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;", false),
//                    toAdd);
            //instructions.remove(new MethodInsnNode(INVOKESTATIC, "joptsimple/internal/Strings","join","(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;", false));

        }
    }
}
