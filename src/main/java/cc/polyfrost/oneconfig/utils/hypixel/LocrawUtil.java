package cc.polyfrost.oneconfig.utils.hypixel;

import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils;

public class LocrawUtil {
    public static final LocrawUtil INSTANCE = new LocrawUtil();

    public LocrawInfo getLocrawInfo() {
        HypixelUtils.Location location = HypixelUtils.getLocation();
        return new LocrawInfo(
                location.getServerName().orElse(""),
                location.getMode().orElse(""),
                location.getMapName().orElse(""),
                serverType(location)
        );
    }

    public boolean isInGame() {
        return HypixelUtils.getLocation().inGame();
    }

    private static String serverType(HypixelUtils.Location location) {
        try {
            return ((java.util.Optional<?>) location.getClass().getMethod("getServerType").invoke(location)).map(Object::toString).orElse("");
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }
}
