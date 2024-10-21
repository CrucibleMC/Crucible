package io.github.crucible.eventfactory;

import com.google.common.collect.Maps;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class PluginClassLoaderFactory {
    private static AtomicInteger IDs = new AtomicInteger();
    private static final HashMap<Method, Class<?>> cache = Maps.newHashMap();
    private static final String HANDLER_DESC = Type.getInternalName(IEventListener.class);
    private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Event.class));

    public IEventListener create(Method method, Object target) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class<?> cls = createWrapper(method);
        if (Modifier.isStatic(method.getModifiers()))
            return (IEventListener)cls.getDeclaredConstructor().newInstance();
        else
            return (IEventListener)cls.getConstructor(Object.class).newInstance(target);
    }

    public Class<?> createWrapper(Method callback) {
        if (cache.containsKey(callback)) {
            return cache.get(callback);
        }

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        boolean isStatic = Modifier.isStatic(callback.getModifiers());
        String name = getUniqueName(callback);
        String desc = name.replace('.', '/');
        String instType = Type.getInternalName(callback.getDeclaringClass());
        String eventType = Type.getInternalName(callback.getParameterTypes()[0]);

        cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, "java/lang/Object", new String[]{HANDLER_DESC});

        cw.visitSource(".dynamic", null);
        {
            if (!isStatic)
                cw.visitField(ACC_PUBLIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", isStatic ? "()V" : "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "invoke", HANDLER_FUNC_DESC, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            if (!isStatic) {
                mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, instType);
            }
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventType);
            mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, instType, callback.getName(), Type.getMethodDescriptor(callback), false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();

        PluginASMClassLoader LOADER = new PluginASMClassLoader(callback.getDeclaringClass().getClassLoader(), callback.getDeclaringClass().getProtectionDomain());
        Class<?> ret = LOADER.define(name, bytes);
        cache.put(callback, ret);
        return ret;
    }

    public String getUniqueName(Method callback) {
        return String.format("%s_%d_%s_%s_%s",
                getClass().getName(),
                IDs.incrementAndGet(),
                callback.getDeclaringClass().getSimpleName(),
                callback.getName(),
                callback.getParameterTypes()[0].getSimpleName());
    }

    private static class PluginASMClassLoader extends ClassLoader {
        private final ProtectionDomain protectionDomain;

        private PluginASMClassLoader(ClassLoader classLoader, ProtectionDomain protectionDomain) {
            super(classLoader);
            this.protectionDomain = protectionDomain;
        }

        public Class<?> define(String name, byte[] data) {
            return defineClass(name, data, 0, data.length, protectionDomain);
        }
    }
}
