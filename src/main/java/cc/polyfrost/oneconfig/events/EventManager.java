package cc.polyfrost.oneconfig.events;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import cc.polyfrost.oneconfig.events.event.LocrawEvent;
import cc.polyfrost.oneconfig.events.event.Stage;
import cc.polyfrost.oneconfig.events.event.Subscribe;
import cc.polyfrost.oneconfig.events.event.TickEvent;
import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;

public final class EventManager {
    public static final EventManager INSTANCE = new EventManager();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private boolean hooked;
    private String lastLocation = "";
    private int ticks;

    private EventManager() {
    }

    public void register(Object target) {
        if (!hooked) {
            hooked = true;
            MinecraftForge.EVENT_BUS.register(new Hook());
        }
        for (Method method : target.getClass().getMethods()) {
            if (method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1) {
                method.setAccessible(true);
                listeners.add(new Listener(target, method));
            }
        }
    }

    public void unregister(Object target) {
        listeners.removeIf(listener -> listener.target == target);
    }

    public void post(Object event) {
        for (Listener listener : listeners) {
            if (!listener.method.getParameterTypes()[0].isInstance(event)) continue;
            try {
                listener.method.invoke(listener.target, event);
            } catch (ReflectiveOperationException e) {
                LogManager.getLogger("ForgeCompat").error("OneConfig event handler {} failed", listener.method, e);
            }
        }
    }

    private record Listener(Object target, Method method) {
    }

    public final class Hook {
        @SubscribeEvent
        public void onTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
            Stage stage = event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START ? Stage.START : Stage.END;
            post(new TickEvent(stage));
            if (stage != Stage.END || ++ticks % 20 != 0 || !HypixelUtils.INSTANCE.isHypixel()) return;
            LocrawInfo info = LocrawUtil.INSTANCE.getLocrawInfo();
            String key = info.getServerId() + "|" + info.getGameMode() + "|" + info.getMapName();
            if (Objects.equals(key, lastLocation)) return;
            lastLocation = key;
            post(new LocrawEvent(info));
        }
    }
}
