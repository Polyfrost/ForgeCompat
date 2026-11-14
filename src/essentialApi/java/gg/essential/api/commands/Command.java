package gg.essential.api.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import kotlin.jvm.internal.DefaultConstructorMarker;
import org.polyfrost.forgecompat.EssentialApi;

public abstract class Command {
    private final String name;

    public Command(String name, boolean autoHelpSubcommand, boolean hideFromAutocomplete) {
        this.name = name;
    }

    public Command(String name, boolean autoHelpSubcommand, boolean hideFromAutocomplete, int mask, DefaultConstructorMarker marker) {
        this(name, autoHelpSubcommand, hideFromAutocomplete);
    }

    public Command(String name, boolean autoHelpSubcommand) {
        this(name, autoHelpSubcommand, false);
    }

    public Command(String name) {
        this(name, true, false);
    }

    public final String getName() {
        return name;
    }

    public final void register() {
        EssentialApi.registerCommand(name, this::run);
    }

    private void run(String[] args) {
        Method handler = null;
        for (Method method : getClass().getDeclaredMethods()) {
            SubCommand sub = method.getAnnotation(SubCommand.class);
            if (sub != null && args.length > 0 && (sub.value().equalsIgnoreCase(args[0]) || Arrays.stream(sub.aliases()).anyMatch(args[0]::equalsIgnoreCase))) {
                handler = method;
                break;
            }
            if (method.isAnnotationPresent(DefaultHandler.class)) handler = method;
        }
        if (handler == null) return;
        try {
            handler.setAccessible(true);
            handler.invoke(this);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
