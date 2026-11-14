package gg.essential.api;

import gg.essential.api.gui.NotificationBuilder;
import gg.essential.api.gui.Notifications;
import gg.essential.api.utils.GuiUtil;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import net.minecraft.client.gui.GuiScreen;
import org.polyfrost.forgecompat.EssentialApi;

final class Shim implements EssentialAPI, Notifications, GuiUtil {
    static final Shim INSTANCE = new Shim();

    @Override
    public Notifications notifications() {
        return this;
    }

    @Override
    public GuiUtil guiUtil() {
        return this;
    }

    @Override
    public void push(String title, String message) {
        push(title, message, 4f, null, null, null);
    }

    @Override
    public void push(String title, String message, float duration, Function0<Unit> action, Function0<Unit> close, Function1<? super NotificationBuilder, Unit> configure) {
        Builder builder = new Builder();
        builder.duration = duration;
        builder.action = action;
        builder.close = close;
        if (configure != null) configure.invoke(builder);
        Function0<Unit> onAction = builder.action;
        EssentialApi.notify(title, message, builder.duration, onAction == null ? null : onAction::invoke);
    }

    @Override
    public void openScreen(GuiScreen screen) {
        EssentialApi.open(screen);
    }

    @Override
    public GuiScreen openedScreen() {
        return (GuiScreen) EssentialApi.screen();
    }

    private static final class Builder implements NotificationBuilder {
        float duration;
        Function0<Unit> action;
        Function0<Unit> close;

        @Override
        public float getDuration() {
            return duration;
        }

        @Override
        public void setDuration(float duration) {
            this.duration = duration;
        }

        @Override
        public Function0<Unit> getOnAction() {
            return action;
        }

        @Override
        public void setOnAction(Function0<Unit> action) {
            this.action = action;
        }

        @Override
        public Function0<Unit> getOnClose() {
            return close;
        }

        @Override
        public void setOnClose(Function0<Unit> close) {
            this.close = close;
        }
    }
}
