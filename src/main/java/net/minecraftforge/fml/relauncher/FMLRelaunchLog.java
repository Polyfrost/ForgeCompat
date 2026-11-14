package net.minecraftforge.fml.relauncher;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class FMLRelaunchLog {
    public static FMLRelaunchLog log = new FMLRelaunchLog();

    public static void log(String target, Level level, String format, Object... data) {
        LogManager.getLogger(target).log(level, String.format(format, data));
    }

    public static void log(Level level, String format, Object... data) {
        log("FML", level, format, data);
    }

    public static void log(Level level, Throwable ex, String format, Object... data) {
        LogManager.getLogger("FML").log(level, String.format(format, data), ex);
    }

    public static void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }

    public static void warning(String format, Object... data) {
        log(Level.WARN, format, data);
    }

    public static void severe(String format, Object... data) {
        log(Level.ERROR, format, data);
    }

    public static void fine(String format, Object... data) {
        log(Level.DEBUG, format, data);
    }
}
