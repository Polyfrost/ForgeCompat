package net.minecraftforge.fml.common.eventhandler;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final Listener[] NONE = new Listener[0];
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    // Replaced, not cleared, after every change so a lookup racing a register can't store a stale array in the live map
    private volatile Map<Class<?>, Listener[]> byType = new ConcurrentHashMap<>();

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
        byType = new ConcurrentHashMap<>();
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
        byType = new ConcurrentHashMap<>();
    }

    /** Lets hot call sites skip building an event nobody receives. */
    public boolean hasListeners(Class<? extends Event> type) {
        return listeners(type).length != 0;
    }

    private Listener[] listeners(Class<?> type) {
        return byType.computeIfAbsent(type, t -> {
            Listener[] matching = listeners.stream().filter(listener -> listener.type.isAssignableFrom(t)).toArray(Listener[]::new);
            return matching.length == 0 ? NONE : matching;
        });
    }

    public boolean post(Event event) {
        for (Listener listener : listeners(event.getClass())) {
            if (!event.isCanceled() || listener.subscribe.receiveCanceled()) {
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
