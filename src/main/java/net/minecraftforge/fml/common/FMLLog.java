package net.minecraftforge.fml.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FMLLog {
    private static final Logger LOGGER = LogManager.getLogger("FML");

    public static void log(String targetLog, Level level, String format, Object... data) {
        LogManager.getLogger(targetLog).log(level, String.format(format, data));
    }

    public static void log(Level level, String format, Object... data) {
        LOGGER.log(level, String.format(format, data));
    }

    public static void log(String targetLog, Level level, Throwable ex, String format, Object... data) {
        LogManager.getLogger(targetLog).log(level, String.format(format, data), ex);
    }

    public static void log(Level level, Throwable ex, String format, Object... data) {
        LOGGER.log(level, String.format(format, data), ex);
    }

    public static void severe(String format, Object... data) {
        log(Level.ERROR, format, data);
    }

    public static void bigWarning(String format, Object... data) {
        LOGGER.warn("****************************************");
        LOGGER.warn("* {}", String.format(format, data));
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < Math.min(8, trace.length); i++) LOGGER.warn("*  at {}", trace[i]);
        LOGGER.warn("****************************************");
    }

    public static void warning(String format, Object... data) {
        log(Level.WARN, format, data);
    }

    public static void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }

    public static void fine(String format, Object... data) {
        log(Level.DEBUG, format, data);
    }

    public static void finer(String format, Object... data) {
        log(Level.TRACE, format, data);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
