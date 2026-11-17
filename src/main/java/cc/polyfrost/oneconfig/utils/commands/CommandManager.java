package cc.polyfrost.oneconfig.utils.commands;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommandGroup;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CommandManager {
    public static final CommandManager INSTANCE = new CommandManager();

    public void registerCommand(Object command) {
        Command meta = command.getClass().getAnnotation(Command.class);
        if (meta == null) throw new IllegalArgumentException(command.getClass().getName() + " is not annotated with @Command");
        ClientCommandHandler.instance.register(new LegacyCommand(meta, command));
    }

    private static final class LegacyCommand extends AbstractCommand {
        private final Command meta;
        private final Object root;

        LegacyCommand(Command meta, Object root) {
            this.meta = meta;
            this.root = root;
        }

        @Override
        public String getName() {
            return meta.value();
        }

        @Override
        public List<String> getAliases() {
            return Arrays.asList(meta.aliases());
        }

        @Override
        public String getUsage(CommandSource source) {
            return "/" + meta.value();
        }

        @Override
        public boolean canUse(CommandSource source) {
            return true;
        }

        @Override
        public void run(CommandSource source, String[] args) throws CommandException {
            Object target = root;
            int i = 0;
            while (i < args.length) {
                Object group = group(target, args[i]);
                if (group == null) break;
                target = group;
                i++;
            }
            Method method = i < args.length ? subCommand(target, args[i]) : null;
            if (method != null) i++;
            else method = mainMethod(target);
            if (method == null) throw new CommandException("Unknown subcommand. Usage: " + usage(target));
            invoke(target, method, Arrays.copyOfRange(args, i, args.length));
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String[] args, BlockPos pos) {
            Object target = root;
            for (int i = 0; i < args.length - 1; i++) {
                target = group(target, args[i]);
                if (target == null) return new ArrayList<>();
            }
            return suggestMatching(args, names(target));
        }

        private static Object group(Object parent, String name) {
            for (Class<?> cls : parent.getClass().getDeclaredClasses()) {
                SubCommandGroup meta = cls.getAnnotation(SubCommandGroup.class);
                if (meta == null || !(meta.value().equalsIgnoreCase(name) || containsIgnoreCase(meta.aliases(), name))) continue;
                try {
                    boolean inner = !Modifier.isStatic(cls.getModifiers());
                    Constructor<?> constructor = inner ? cls.getDeclaredConstructor(parent.getClass()) : cls.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return inner ? constructor.newInstance(parent) : constructor.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to create subcommand group " + cls.getName(), e);
                }
            }
            return null;
        }

        private static Method subCommand(Object target, String name) {
            for (Method method : target.getClass().getDeclaredMethods()) {
                SubCommand meta = method.getAnnotation(SubCommand.class);
                if (meta != null && (method.getName().equalsIgnoreCase(name) || containsIgnoreCase(meta.aliases(), name))) return method;
            }
            return null;
        }

        private static Method mainMethod(Object target) {
            for (Method method : target.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Main.class)) return method;
            }
            return null;
        }

        private static List<String> names(Object target) {
            List<String> names = new ArrayList<>();
            for (Method method : target.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubCommand.class)) names.add(method.getName().toLowerCase(Locale.ROOT));
            }
            for (Class<?> cls : target.getClass().getDeclaredClasses()) {
                SubCommandGroup meta = cls.getAnnotation(SubCommandGroup.class);
                if (meta != null) names.add(meta.value());
            }
            return names;
        }

        private static String usage(Object target) {
            return String.join(", ", names(target));
        }

        private static void invoke(Object target, Method method, String[] args) throws CommandException {
            Class<?>[] types = method.getParameterTypes();
            if (args.length < types.length || (args.length > types.length && (types.length == 0 || types[types.length - 1] != String.class))) {
                throw new CommandException("Expected " + types.length + " argument(s) for " + method.getName());
            }
            Object[] values = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                // a trailing String takes the rest of the input, so multi-word arguments work
                String arg = i == types.length - 1 && types[i] == String.class ? String.join(" ", Arrays.copyOfRange(args, i, args.length)) : args[i];
                values[i] = parse(types[i], arg);
            }
            try {
                method.setAccessible(true);
                method.invoke(target, values);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Command " + method.getName() + " failed", e.getCause());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private static Object parse(Class<?> type, String arg) throws CommandException {
            try {
                if (type == String.class) return arg;
                if (type == int.class || type == Integer.class) return Integer.parseInt(arg);
                if (type == long.class || type == Long.class) return Long.parseLong(arg);
                if (type == double.class || type == Double.class) return Double.parseDouble(arg);
                if (type == float.class || type == Float.class) return Float.parseFloat(arg);
                if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(arg);
            } catch (NumberFormatException e) {
                throw new CommandException("'" + arg + "' is not a valid " + type.getSimpleName());
            }
            throw new CommandException("Unsupported argument type " + type.getSimpleName());
        }

        private static boolean containsIgnoreCase(String[] values, String name) {
            for (String value : values) if (value.equalsIgnoreCase(name)) return true;
            return false;
        }
    }
}
