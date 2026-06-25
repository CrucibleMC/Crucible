package io.github.crucible.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Recomputes StackMapTables ({@code COMPUTE_FRAMES}) as the <b>last</b> transform-chain step so classes
 * left with stale frames by upstream {@code COMPUTE_MAXS} transformers pass the split bytecode
 * verifier. Uses the deobf-aware, never-throwing {@link SafeAsmClassWriter}. Runs last by self-reordering
 * to the tail of the LaunchClassLoader transformer list (whole-list copy-and-swap). Never breaks a class:
 * any failure returns the input unchanged.
 *
 * <p>This complements RFB's {@code rfb-asm-safety} plugin, which only makes {@code getCommonSuperClass}
 * safe <em>when a transformer already recomputes frames</em>; it does not force a recompute on a class
 * (like Cauldron's patched {@code ChunkProviderServer.func_73153_a}) whose stale StackMapTable was left by
 * a {@code COMPUTE_MAXS}-only coremod/Mixin pass. Reflectively loading that class (e.g. Dynmap's field
 * scan) throws {@code VerifyError: Expecting a stackmap frame at branch target N}. Measured on the
 * lwjgl3ify server path: the full pack fails this way on every modern JDK tested (21-25); Java 8 is
 * unaffected because its verifier fails over to the old type-inference verifier for these class files.
 * This transformer forces the repair.
 *
 * <p>Set {@code -Dcrucible.frameSafety.debug=true} to log every recompute outcome and dump the pre/post
 * bytes of classes named in {@code -Dcrucible.frameSafety.debugClass=...} (default: ChunkProviderServer)
 * to /tmp. Disable the whole transformer with {@code -Dcrucible.frameSafety=false}.
 */
public final class AsmFrameSafetyTransformer implements IClassTransformer {

    private static final Field TRANSFORMERS_FIELD = resolveTransformersField();
    private static final boolean DEBUG = Boolean.getBoolean("crucible.frameSafety.debug");
    private static final String DEBUG_CLASS =
        System.getProperty("crucible.frameSafety.debugClass", "ChunkProviderServer");
    /**
     * Only recompute classes whose (deobf) name starts with one of these comma-separated prefixes —
     * the stale frames the verifier rejects come from COMPUTE_MAXS-only Mixin/coremod passes,
     * which overwhelmingly target {@code net.minecraft.*}. Recomputing every class would needlessly read+
     * rewrite thousands of mod classes at load time. Set {@code -Dcrucible.frameSafety.scope=all} (or empty)
     * to recompute everything, or a custom prefix list to widen (e.g. to a mod package that also breaks).
     */
    private static final String[] SCOPE = parseScope(
        System.getProperty("crucible.frameSafety.scope", "net.minecraft."));

    private static String[] parseScope(String raw) {
        if (raw == null) return new String[0];
        raw = raw.trim();
        if (raw.isEmpty() || raw.equalsIgnoreCase("all")) return new String[0]; // empty = everything
        String[] parts = raw.split(",");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

    private static boolean inScope(String transformedName) {
        if (SCOPE.length == 0) return true; // "all"
        if (transformedName == null) return false;
        for (String p : SCOPE) if (!p.isEmpty() && transformedName.startsWith(p)) return true;
        return false;
    }

    private static Field resolveTransformersField() {
        try {
            Field f = Launch.classLoader.getClass().getDeclaredField("transformers");
            f.setAccessible(true);
            return f;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ensureRunsLast();
        if (basicClass == null) return null;
        // never recompute our own writer/transformer, nor the external lwjgl3ify classes (which carry
        // transformerExclusions and must reach the verifier untouched).
        if (name != null && (name.startsWith("io.github.crucible.asm")
            || name.startsWith("me.eigenraven.lwjgl3ify"))) return basicClass;
        if (!inScope(transformedName)) return basicClass;

        final boolean dbg = DEBUG && transformedName != null && transformedName.contains(DEBUG_CLASS);
        if (dbg) dump(transformedName + ".pre", basicClass);
        try {
            ClassReader reader = new ClassReader(basicClass);
            SafeAsmClassWriter writer = new SafeAsmClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            reader.accept(writer, 0);
            byte[] out = writer.toByteArray();
            if (dbg) {
                dump(transformedName + ".post", out);
                System.out.println("[frameSafety] recomputed OK: " + transformedName);
            }
            return out;
        } catch (Throwable t) {
            if (dbg) {
                System.out.println("[frameSafety] recompute THREW for " + transformedName + ": " + t);
                t.printStackTrace(System.out);
            }
            return basicClass;
        }
    }

    private static void dump(String tag, byte[] bytes) {
        try {
            File f = new File("/tmp/framedbg-" + tag.replace('/', '.') + ".class");
            FileOutputStream fos = new FileOutputStream(f);
            try {
                fos.write(bytes);
            } finally {
                fos.close();
            }
            System.out.println("[frameSafety] dumped " + f + " (" + bytes.length + " bytes)");
        } catch (Throwable ignored) {
            // no-op
        }
    }

    @SuppressWarnings("unchecked")
    private void ensureRunsLast() {
        final Field f = TRANSFORMERS_FIELD;
        if (f == null) return;
        try {
            final List<IClassTransformer> current = (List<IClassTransformer>) f.get(Launch.classLoader);
            if (current == null || current.isEmpty()) return;
            if (current.get(current.size() - 1) == this) return;
            final int idx = current.indexOf(this);
            if (idx < 0) return;
            final List<IClassTransformer> next = new ArrayList<IClassTransformer>(current.size());
            for (int i = 0; i < current.size(); i++) {
                if (i != idx) next.add(current.get(i));
            }
            next.add(this);
            f.set(Launch.classLoader, next);
        } catch (Throwable ignored) {
            // best-effort
        }
    }
}
