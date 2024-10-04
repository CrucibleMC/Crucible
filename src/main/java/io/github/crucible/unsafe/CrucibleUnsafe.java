package io.github.crucible.unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

@SuppressWarnings({ "restriction", "sunapi" })
public class CrucibleUnsafe {

    private static final sun.misc.Unsafe unsafe;
    private static MethodHandles.Lookup lookup;
    private static MethodHandle defineClass;

    static {
        try {
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (sun.misc.Unsafe) theUnsafe.get(null);

            try {
                MethodHandles.Lookup.class.getDeclaredMethod("ensureInitialized", MethodHandles.Lookup.class)
                        .invoke(MethodHandles.lookup(), MethodHandles.Lookup.class);
                Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                Object base = unsafe.staticFieldBase(field);
                long offset = unsafe.staticFieldOffset(field);
                lookup = (MethodHandles.Lookup) unsafe.getObject(base, offset);
                MethodHandle mh;
                try {
                    Method sunMisc = unsafe.getClass().getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                    mh = lookup.unreflect(sunMisc).bindTo(unsafe);
                } catch (Exception e) {
                    Class<?> jdkInternalUnsafe = Class.forName("jdk.internal.misc.Unsafe");
                    Field internalUnsafeField = jdkInternalUnsafe.getDeclaredField("theUnsafe");
                    Object internalUnsafe = unsafe.getObject(unsafe.staticFieldBase(internalUnsafeField), unsafe.staticFieldOffset(internalUnsafeField));
                    Method internalDefineClass = jdkInternalUnsafe.getMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                    mh = lookup.unreflect(internalDefineClass).bindTo(internalUnsafe);
                }
                defineClass = mh;
            }catch (Exception ignored){
                //This will probably fail on older java versions, but whatever, we don't need it on older versions
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> defineClass(String s, byte[] bytes, int i, int i1, ClassLoader classLoader, ProtectionDomain protectionDomain) {
        try {
            if (defineClass == null){
                return unsafe.defineClass(s, bytes, i, i1, classLoader, protectionDomain);
            }else {
                return (Class<?>) defineClass.invokeExact(s, bytes, i, i1, classLoader, protectionDomain);
            }
        } catch (Throwable throwable) {
            throwException(throwable);
            return null;
        }
    }

    public static void throwException(Throwable throwable) {
        unsafe.throwException(throwable);
    }

}
