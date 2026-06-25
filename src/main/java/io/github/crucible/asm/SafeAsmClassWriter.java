package io.github.crucible.asm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * A {@link ClassWriter} whose {@link #getCommonSuperClass} resolves the type hierarchy without ever
 * loading a class through {@link Class#forName} that could be duplicate-defined mid-transform, yet still
 * resolves JDK types correctly. This is what lets {@code COMPUTE_FRAMES} repair the StackMapTables that
 * upstream {@code COMPUTE_MAXS} transformers (FML / Mixin / pack coremods) leave invalid for the split
 * bytecode verifier.
 *
 * <p>Resolution strategy per type, in order:
 * <ol>
 *   <li><b>{@code java.*} / {@code javax.*} / {@code sun.*}</b> &rarr; {@code Class.forName} on the bootstrap/
 *       system loader. These are already loaded, never transformed, and live on the bootstrap classpath
 *       where {@code Launch.classLoader.getResourceAsStream} can't see them &mdash; so a bytecode-only
 *       resolver silently fails on {@code java.lang.IllegalStateException} and merges exception types
 *       down to {@code Object}, which breaks MixinExtras' catch frames. forName is safe here precisely
 *       because these aren't the MC/mod classes that duplicate-define during transform.</li>
 *   <li><b>Everything else</b> (MC + mods) &rarr; read the class <em>header</em> (super + interfaces) from the
 *       launch classloader's bytes (deobf-aware: SRG&harr;obf via FMLDeobfuscatingRemapper). Header-only, so it
 *       never links/initializes the class.</li>
 *   <li><b>Unresolvable</b> &rarr; {@code java/lang/Object}. We never throw: throwing would abort the whole
 *       recompute and return the class with its broken frames intact.</li>
 * </ol>
 */
public final class SafeAsmClassWriter extends ClassWriter {

    private static final Map<String, Node> CACHE = new HashMap<String, Node>();

    private static final Object REMAPPER;
    private static final Method MAP; // obf -> srg
    private static final Method UNMAP; // srg -> obf

    static {
        Object instance = null;
        Method map = null, unmap = null;
        try {
            Class<?> c = Class.forName("cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper");
            instance = c.getField("INSTANCE").get(null);
            map = c.getMethod("map", String.class);
            unmap = c.getMethod("unmap", String.class);
        } catch (Throwable t) {
            instance = null;
        }
        REMAPPER = instance;
        MAP = map;
        UNMAP = unmap;
    }

    /**
     * NOTE: deliberately does <b>not</b> pass the {@link ClassReader} to {@code super}. ASM's
     * {@code ClassWriter(ClassReader, flags)} enables an optimization that copies unmodified methods
     * <em>byte-for-byte</em> from the reader &mdash; including their original StackMapTable &mdash; which silently
     * bypasses {@code COMPUTE_FRAMES} (the recompute becomes a no-op; input == output). Using
     * {@code super(flags)} forces ASM to recompute frames for every method from scratch, which is the
     * whole point of this writer.
     */
    public SafeAsmClassWriter(ClassReader reader, int flags) {
        super(flags);
    }

    private static String remap(Method m, String name) {
        if (REMAPPER == null || m == null || name == null) return name;
        try {
            Object r = m.invoke(REMAPPER, name);
            return r != null ? (String) r : name;
        } catch (Throwable t) {
            return name;
        }
    }

    private static boolean isJdk(String internal) {
        return internal.startsWith("java/") || internal.startsWith("javax/") || internal.startsWith("sun/")
            || internal.startsWith("jdk/") || internal.startsWith("com/sun/");
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        if (type1.equals(type2)) return type1;
        if (type1.equals("java/lang/Object") || type2.equals("java/lang/Object")) return "java/lang/Object";

        // Fast path: both JDK types -> use real reflection (safe; they're loaded & never transformed).
        if (isJdk(type1) && isJdk(type2)) {
            String r = jdkCommonSuper(type1, type2);
            if (r != null) return r;
        }

        final Node n1 = info(type1);
        final Node n2 = info(type2);
        if (n1 == null || n2 == null) return "java/lang/Object"; // never throw

        if (n1.isAssignableFrom(n2)) return type1;
        if (n2.isAssignableFrom(n1)) return type2;
        if (n1.isInterface || n2.isInterface) return "java/lang/Object";
        Node c = n1;
        do {
            c = c.superClass;
            if (c == null) return "java/lang/Object";
        } while (!c.isAssignableFrom(n2));
        return c.name;
    }

    /** Common superclass of two JDK types via the real classloader (they can't duplicate-define). */
    private static String jdkCommonSuper(String t1, String t2) {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Class<?> c1 = Class.forName(t1.replace('/', '.'), false, cl);
            Class<?> c2 = Class.forName(t2.replace('/', '.'), false, cl);
            if (c1.isAssignableFrom(c2)) return t1;
            if (c2.isAssignableFrom(c1)) return t2;
            if (c1.isInterface() || c2.isInterface()) return "java/lang/Object";
            Class<?> c = c1;
            do {
                c = c.getSuperclass();
                if (c == null) return "java/lang/Object";
            } while (!c.isAssignableFrom(c2));
            return c.getName().replace('.', '/');
        } catch (Throwable t) {
            return null;
        }
    }

    private static Node info(final String type) {
        synchronized (CACHE) {
            if (CACHE.containsKey(type)) return CACHE.get(type);
        }
        Node node = build(type);
        synchronized (CACHE) {
            CACHE.put(type, node);
        }
        return node;
    }

    private static Node build(final String type) {
        // JDK types: resolve header via reflection (bootstrap classpath isn't visible as a resource).
        if (isJdk(type)) {
            try {
                Class<?> k = Class.forName(type.replace('/', '.'), false, ClassLoader.getSystemClassLoader());
                Node sup = k.getSuperclass() == null ? null : info(k.getSuperclass().getName().replace('.', '/'));
                Set<Node> supers = new HashSet<Node>();
                if (sup != null) supers.addAll(sup.allSupers);
                for (Class<?> itf : k.getInterfaces()) {
                    Node in = info(itf.getName().replace('.', '/'));
                    if (in != null) supers.addAll(in.allSupers);
                }
                return new Node(type, sup, k.isInterface(), supers);
            } catch (Throwable t) {
                return null;
            }
        }
        final byte[] bytes = bytesFor(type);
        if (bytes == null) return null;
        final ClassReader cr;
        try {
            cr = new ClassReader(bytes);
        } catch (Throwable t) {
            return null;
        }
        final Set<Node> supers = new HashSet<Node>();
        Node superNode = null;
        final String superObf = cr.getSuperName();
        if (superObf != null) {
            superNode = info(remap(MAP, superObf));
            if (superNode != null) supers.addAll(superNode.allSupers);
        }
        for (final String itfObf : cr.getInterfaces()) {
            final Node in = info(remap(MAP, itfObf));
            if (in != null) supers.addAll(in.allSupers);
        }
        final boolean isInterface = (cr.getAccess() & Opcodes.ACC_INTERFACE) != 0;
        return new Node(type, superNode, isInterface, supers);
    }

    private static byte[] bytesFor(final String srgInternalName) {
        try {
            final byte[] b = Launch.classLoader.getClassBytes(srgInternalName.replace('/', '.'));
            if (b != null) return b;
        } catch (Throwable ignored) {
            // fall through
        }
        final String obf = remap(UNMAP, srgInternalName);
        InputStream is = null;
        try {
            is = Launch.classLoader.getResourceAsStream(obf + ".class");
            if (is == null && !obf.equals(srgInternalName)) {
                is = Launch.classLoader.getResourceAsStream(srgInternalName + ".class");
            }
            if (is != null) return readAll(is);
        } catch (Throwable ignored) {
            // fall through
        } finally {
            if (is != null) try {
                is.close();
            } catch (Throwable ignored2) {
                /* no-op */
            }
        }
        return null;
    }

    private static byte[] readAll(final InputStream is) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        final byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    private static final class Node {
        final String name;
        final Node superClass;
        final boolean isInterface;
        final Set<Node> allSupers;

        Node(String name, Node superClass, boolean isInterface, Set<Node> allSupers) {
            this.name = name;
            this.superClass = superClass;
            this.isInterface = isInterface;
            this.allSupers = allSupers;
            allSupers.add(this);
        }

        boolean isAssignableFrom(Node other) {
            return other.allSupers.contains(this);
        }
    }
}
