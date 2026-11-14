package gg.essential.api.gui;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public interface Notifications {
    void push(String title, String message);

    void push(String title, String message, float duration, Function0<Unit> action, Function0<Unit> close, Function1<? super NotificationBuilder, Unit> configure);

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void push$default(Notifications notifications, String title, String message, float duration, Function0 action, Function0 close, Function1 configure, int mask, Object marker) {
        notifications.push(title, message, (mask & 4) != 0 ? 4f : duration, (mask & 8) != 0 ? null : action, (mask & 16) != 0 ? null : close, (mask & 32) != 0 ? null : configure);
    }
}
