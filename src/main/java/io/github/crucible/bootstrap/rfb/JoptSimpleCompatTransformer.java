package io.github.crucible.bootstrap.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import java.util.jar.Manifest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Early library compatibility patch in the style of lwjgl3ify/RFB transformers.
 *
 * jopt-simple 5.x replaced the 4.x signature
 * {@code Strings.join(List, String)} with {@code Strings.join(Iterable, String)},
 * so any mod compiled against the 4.x signature (e.g. RecurrentComplex) fails
 * with {@link NoSuchMethodError} at runtime, because Crucible ships jopt-simple 5.0.1.
 *
 * This transformer injects a synthetic {@code join(List, String)} overload delegating
 * to the 5.x {@code join(Iterable, String)}, restoring binary compatibility for both
 * signatures no matter which mod calls them. It applies to every copy of the class,
 * on both the system classloader and the LaunchClassLoader.
 */
public class JoptSimpleCompatTransformer implements RfbClassTransformer {
    public static final Logger LOGGER = LogManager.getLogger("Crucible-JoptCompat");

    private static final String TARGET_CLASS = "joptsimple.internal.Strings";
    private static final String JOIN_LIST_DESC = "(Ljava/util/List;Ljava/lang/String;)Ljava/lang/String;";
    private static final String JOIN_ITERABLE_DESC = "(Ljava/lang/Iterable;Ljava/lang/String;)Ljava/lang/String;";
    private static final String OWNER = "joptsimple/internal/Strings";

    @Override
    public @NotNull String id() {
        return "crucible-jopt-simple-compat";
    }

    @Override
    public boolean shouldTransformClass(
            @NotNull ExtensibleClassLoader classLoader,
            @NotNull Context context,
            @Nullable Manifest manifest,
            @NotNull String className,
            @NotNull ClassNodeHandle classNode) {
        // Cheap name check only - avoid triggering the lazy ASM parse for other classes
        return className.equals(TARGET_CLASS) && classNode.isPresent();
    }

    @Override
    public boolean transformClassIfNeeded(
            @NotNull ExtensibleClassLoader classLoader,
            @NotNull Context context,
            @Nullable Manifest manifest,
            @NotNull String className,
            @NotNull ClassNodeHandle classNode) {
        final ClassNode node = classNode.getNode();
        if (node == null || node.methods == null) {
            return false;
        }

        boolean hasListOverload = false;
        boolean hasIterableOverload = false;
        for (final MethodNode method : node.methods) {
            if (!method.name.equals("join")) {
                continue;
            }
            if (method.desc.equals(JOIN_LIST_DESC)) {
                hasListOverload = true;
            } else if (method.desc.equals(JOIN_ITERABLE_DESC)) {
                hasIterableOverload = true;
            }
        }

        if (hasListOverload || !hasIterableOverload) {
            // 4.x already provides the List signature, or this jopt-simple build is
            // too different to bridge - leave it alone.
            return false;
        }

        node.methods.add(generateBridgeMethod());
        // The added method needs its stack/locals sizes computed when the class is written
        classNode.computeMaxs();
        LOGGER.info("Injected missing joptsimple.internal.Strings.join(List, String) bridge into {} copy",
                context.name());
        return true;
    }

    /**
     * Generates: {@code public static String join(List parts, String sep) { return join((Iterable) parts, sep); }}
     * Straight-line bytecode, so no stack map frames are required regardless of the class file version.
     */
    private static MethodNode generateBridgeMethod() {
        final MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "join", JOIN_LIST_DESC, null, null);
        final InsnList insns = method.instructions;
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, "join", JOIN_ITERABLE_DESC, false));
        insns.add(new InsnNode(Opcodes.ARETURN));
        return method;
    }
}
