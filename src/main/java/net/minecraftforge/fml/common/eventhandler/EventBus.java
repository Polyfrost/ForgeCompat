package net.minecraftforge.fml.common.eventhandler;

import org.apache.logging.log4j.LogManager;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
                listeners.add(new Listener(target, method.getParameterTypes()[0], subscribe, invoker(target, method)));
            }
        }
        listeners.sort(Comparator.comparing(listener -> listener.subscribe.priority()));
        byType = new ConcurrentHashMap<>();
    }

    private static Method[] declaredMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                methods.addAll(Arrays.asList(c.getDeclaredMethods()));
            } catch (LinkageError e) {
                LogManager.getLogger("ForgeCompat").error("Skipping event handlers of {}: {}", c.getName(), e.toString());
            }
        }
        return methods.stream().filter(m -> Modifier.isPublic(m.getModifiers())).toArray(Method[]::new);
    }

    private static IEventListener invoker(Object target, Method method) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
            MethodHandle handle = lookup.unreflect(method);
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            MethodType factoryType = isStatic ? MethodType.methodType(IEventListener.class) : MethodType.methodType(IEventListener.class, method.getDeclaringClass());
            MethodHandle factory = LambdaMetafactory.metafactory(lookup, "invoke", factoryType,
                    MethodType.methodType(void.class, Event.class), handle,
                    MethodType.methodType(void.class, method.getParameterTypes()[0])).getTarget();
            return isStatic ? (IEventListener) factory.invoke() : (IEventListener) factory.invoke(target);
        } catch (Throwable e) {
            LogManager.getLogger("ForgeCompat").debug("Using reflection for event handler {}: {}", method, e.toString());
            return event -> {
                try {
                    method.invoke(target, event);
                } catch (ReflectiveOperationException ex) {
                    throw new RuntimeException("Forge event handler " + method + " failed", ex);
                }
            };
        }
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
                listener.invoker.invoke(event);
            }
        }
        return event.isCanceled();
    }

    private record Listener(Object target, Class<?> type, SubscribeEvent subscribe, IEventListener invoker) {
    }
}
