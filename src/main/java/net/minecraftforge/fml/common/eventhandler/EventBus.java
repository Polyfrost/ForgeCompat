package net.minecraftforge.fml.common.eventhandler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final List<Object[]> listeners = new CopyOnWriteArrayList<>();

    public void register(Object target) {
        for (Method method : target.getClass().getMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class) && method.getParameterCount() == 1) {
                listeners.add(new Object[]{target, method});
            }
        }
    }

    public boolean post(Event event) {
        for (Object[] listener : listeners) {
            Method method = (Method) listener[1];
            if (method.getParameterTypes()[0].isInstance(event)) {
                try {
                    method.invoke(listener[0], event);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Forge event handler " + method + " failed", e);
                }
            }
        }
        return false;
    }
}
