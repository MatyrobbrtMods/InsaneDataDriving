package com.matyrobbrt.idd.util;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Reflections {
    public static final MethodHandles.Lookup LOOKUP;
    public static final Unsafe UNSAFE;
    static {
        try {
            final var unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe) unsafeField.get(null);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        try {
            LOOKUP = (MethodHandles.Lookup) UNSAFE.getObject(MethodHandles.Lookup.class, UNSAFE.staticFieldOffset(MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP")));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static final VarHandle WRAPPED_BUILDER = findFieldOf(
            RecordCodecBuilder.build(allocate(RecordCodecBuilder.class)).getClass(), "val$builder", RecordCodecBuilder.class, false
    );

    public static VarHandle findFieldOf(Class<?> clazz, String name, Class<?> type, boolean isStatic) {
        try {
            if (isStatic) {
                return LOOKUP.findStaticVarHandle(clazz, name, type);
            } else {
                return LOOKUP.findVarHandle(clazz, name, type);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Object> getFields(Object object) {
        try {
            final Map<String, Object> fields = new HashMap<>();
            for (Field declaredField : object.getClass().getDeclaredFields()) {
                final Object o = LOOKUP.unreflectGetter(declaredField).invoke(object);
                if (o.getClass() != String.class) {
                    final var m = getFields(o);
                    m.put("#root", o);
                    fields.put(declaredField.getName(), m);
                } else {
                    fields.put(declaredField.getName(), o);
                }
            }
            return fields;
        } catch (Throwable exception) {
            throw new RuntimeException(exception);
        }
    }

    public static <T> T getField(String name, Object object) {
        try {
            final Field f = object.getClass().getDeclaredField(name);
            return (T) LOOKUP.unreflectGetter(f).invoke(object);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Object invokeMethod(Object target, String name, Object[] args) {
        for (final Method method : target.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                try {
                    return method.invoke(target, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static <T> T allocate(Class<T> type) {
        try {
            return (T) UNSAFE.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field fieldOrNull(Class<?> type, String name) {
        try {
            return type.getDeclaredField(name);
        } catch (Exception ex) {
            return null;
        }
    }

    public static VarHandle fieldHandle(Class<?> owner, String name) {
        try {
            return LOOKUP.unreflectVarHandle(owner.getDeclaredField(name));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
