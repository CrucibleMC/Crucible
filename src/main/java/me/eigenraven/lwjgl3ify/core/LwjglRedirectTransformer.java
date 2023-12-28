package me.eigenraven.lwjgl3ify.core;

import io.github.crucible.CrucibleModContainer;
import io.github.crucible.bootstrap.CrucibleMetadata;
import me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class LwjglRedirectTransformer extends Remapper implements IClassTransformer {

    int remaps = 0, calls = 0;

    public static LwjglRedirectTransformer activeInstance = null;

    public LwjglRedirectTransformer() {
        // Only use the last constructed transformer
        activeInstance = this;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (this != activeInstance) {
            return basicClass;
        }
        if (basicClass == null) {
            return null;
        }
        if (name.contains("lwjgl3ify")) {
            return basicClass;
        }
        ClassReader reader = new ClassReader(basicClass);
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = new EscapingClassRemapper(writer);

        try {
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } catch (Lwjgl3AwareException e) {
            return basicClass;
        } catch (Exception e) {
            CrucibleModContainer.logger.warn("Couldn't remap class {}", transformedName, e);
            return basicClass;
        }

        return writer.toByteArray();
    }

    final String[] fromPrefixes = new String[] { "org/lwjgl/", "javax/xml/bind/", "javax/servlet/" };

    final String[] toPrefixes = new String[] { "org/lwjglx/", "jakarta/xml/bind/", "jakarta/servlet/" };

    @Override
    public String map(String typeName) {
        if (typeName == null) {
            return null;
        }
        calls++;
        for (int pfx = 0; pfx < fromPrefixes.length; pfx++) {
            if (typeName.startsWith(fromPrefixes[pfx])) {
                remaps++;
                return toPrefixes[pfx] + typeName.substring(fromPrefixes[pfx].length());
            }
        }

        return typeName;
    }

    public static class Lwjgl3AwareException extends RuntimeException {
    }

    public class EscapingClassRemapper extends ClassRemapper {

        public EscapingClassRemapper(ClassWriter writer) {
            super(writer, LwjglRedirectTransformer.this);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(Type.getDescriptor(Lwjgl3Aware.class))) {
                throw new Lwjgl3AwareException();
            }
            return super.visitAnnotation(desc, visible);
        }
    }
}