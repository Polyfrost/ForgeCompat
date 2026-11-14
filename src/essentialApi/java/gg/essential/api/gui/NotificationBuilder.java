package gg.essential.api.gui;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public interface NotificationBuilder {
    float getDuration();

    void setDuration(float duration);

    Function0<Unit> getOnAction();

    void setOnAction(Function0<Unit> action);

    Function0<Unit> getOnClose();

    void setOnClose(Function0<Unit> close);
}
