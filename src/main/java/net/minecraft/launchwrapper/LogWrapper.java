package net.minecraft.launchwrapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogWrapper {
    public static LogWrapper log = new LogWrapper();
    private final Logger logger = LogManager.getLogger("LaunchWrapper");

    public static void log(String logChannel, Level level, String format, Object... data) {
        LogManager.getLogger(logChannel).log(level, String.format(format, data));
    }

    public static void log(Level level, String format, Object... data) {
        log.logger.log(level, String.format(format, data));
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
