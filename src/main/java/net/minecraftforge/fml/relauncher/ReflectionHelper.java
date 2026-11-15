package net.minecraftforge.fml.relauncher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.polyfrost.forgecompat.Hooks;

public class ReflectionHelper {
    public static Field findField(Class<?> owner, String... names) {
        Exception failed = null;
        for (String name : names) {
            try {
                Field field = Hooks.getDeclaredField(owner, name);
                field.setAccessible(true);
                return field;
            } catch (Exception e) {
                failed = e;
            }
        }
        throw new UnableToFindFieldException(names, failed);
    }

    @SuppressWarnings("unchecked")
    public static <T, E> T getPrivateValue(Class<? super E> owner, E instance, String... names) {
        try {
            return (T) findField(owner, names).get(instance);
        } catch (ReflectiveOperationException e) {
            throw new UnableToAccessFieldException(names, e);
        }
    }

    public static <T, E> void setPrivateValue(Class<? super T> owner, T instance, E value, String... names) {
        try {
            findField(owner, names).set(instance, value);
        } catch (ReflectiveOperationException e) {
            throw new UnableToAccessFieldException(names, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, E> T getPrivateValue(Class<? super E> owner, E instance, int fieldIndex) {
        try {
            Field field = owner.getDeclaredFields()[fieldIndex];
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            throw new UnableToAccessFieldException(new String[0], e);
        }
    }

    public static <T, E> void setPrivateValue(Class<? super T> owner, T instance, E value, int fieldIndex) {
        try {
            Field field = owner.getDeclaredFields()[fieldIndex];
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new UnableToAccessFieldException(new String[0], e);
        }
    }

    public static Class<? super Object> getClass(ClassLoader loader, String... classNames) {
        Exception failed = null;
        for (String name : classNames) {
            try {
                @SuppressWarnings("unchecked")
                Class<? super Object> type = (Class<? super Object>) Hooks.forName(name, false, loader);
                return type;
            } catch (Exception e) {
                failed = e;
            }
        }
        throw new UnableToFindClassException(classNames, failed);
    }

    public static <E> Method findMethod(Class<? super E> owner, E instance, String[] names, Class<?>... params) {
        Exception failed = null;
        for (String name : names) {
            try {
                Method method = owner.getDeclaredMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (Exception e) {
                failed = e;
            }
        }
        throw new UnableToFindMethodException(names, failed);
    }

    public static class UnableToFindFieldException extends RuntimeException {
        public final String[] fieldNames;

        public UnableToFindFieldException(String[] fieldNames, Exception cause) {
            super(cause);
            this.fieldNames = fieldNames;
        }
    }

    public static class UnableToAccessFieldException extends RuntimeException {
        public final String[] fieldNames;

        public UnableToAccessFieldException(String[] fieldNames, Exception cause) {
            super(cause);
            this.fieldNames = fieldNames;
        }
    }

    public static class UnableToFindClassException extends RuntimeException {
        public final String[] classNames;

        public UnableToFindClassException(String[] classNames, Exception cause) {
            super(cause);
            this.classNames = classNames;
        }
    }

    public static class UnableToFindMethodException extends RuntimeException {
        public final String[] methodNames;

        public UnableToFindMethodException(String[] methodNames, Exception cause) {
            super(cause);
            this.methodNames = methodNames;
        }
    }
}
