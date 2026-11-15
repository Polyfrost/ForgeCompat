package net.minecraftforge.fml.common.eventhandler;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Event {
    private boolean canceled;
    private Result result = Result.DEFAULT;

    public boolean isCancelable() {
        return hasAnnotation(Cancelable.class);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean cancel) {
        canceled = cancel;
    }

    public boolean hasResult() {
        return hasAnnotation(HasResult.class);
    }

    private boolean hasAnnotation(Class<? extends Annotation> annotation) {
        for (Class<?> type = getClass(); type != Event.class; type = type.getSuperclass()) {
            if (type.isAnnotationPresent(annotation)) return true;
        }
        return false;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result value) {
        result = value;
    }

    public enum Result {
        DENY,
        DEFAULT,
        ALLOW
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface HasResult {
    }
}
