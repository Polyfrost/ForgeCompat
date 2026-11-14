package net.minecraftforge.fml.common.eventhandler;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void register(Object target) {
        Method[] methods;
        try {
            methods = target.getClass().getMethods();
        } catch (LinkageError e) {
            methods = declaredMethods(target.getClass());
        }
        for (Method method : methods) {
            SubscribeEvent subscribe = method.getAnnotation(SubscribeEvent.class);
            if (subscribe != null && method.getParameterCount() == 1) {
                method.setAccessible(true);
                listeners.add(new Listener(target, method, method.getParameterTypes()[0], subscribe));
            }
        }
        listeners.sort(Comparator.comparing(listener -> listener.subscribe.priority()));
    }

    private static Method[] declaredMethods(Class<?> type) {
        List<Method> methods = new java.util.ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                methods.addAll(java.util.Arrays.asList(c.getDeclaredMethods()));
            } catch (LinkageError e) {
                org.apache.logging.log4j.LogManager.getLogger("ForgeCompat").error("Skipping event handlers of {}: {}", c.getName(), e.toString());
            }
        }
        return methods.stream().filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers())).toArray(Method[]::new);
    }

    public void unregister(Object target) {
        listeners.removeIf(listener -> listener.target == target);
    }

    public boolean post(Event event) {
        for (Listener listener : listeners) {
            if (listener.type.isInstance(event) && (!event.isCanceled() || listener.subscribe.receiveCanceled())) {
                try {
                    listener.method.invoke(listener.target, event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Forge event handler " + listener.method + " failed", e);
                }
            }
        }
        return event.isCanceled();
    }

    private record Listener(Object target, Method method, Class<?> type, SubscribeEvent subscribe) {
    }
}
