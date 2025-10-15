package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import net.dreampixel.dreamlevels.DreamLevels;
import top.shadowpixel.shadowcore.util.logging.Logger;

@UtilityClass
public class Debugger {
    private static final Logger logger = DreamLevels.getInstance().logger;

    public static void info(String... messages) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.info(messages);
    }

    public static void warn(String... messages) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.warn(messages);
    }

    public static void warn(String message, Throwable throwable) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.warn(message, throwable);
    }

    public static void warn(Throwable... throwable) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.warn(throwable);
    }

    public static void error(String... messages) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.error(messages);
    }

    public static void error(String message, Throwable throwable) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.error(message, throwable);
    }

    public static void error(Throwable... throwable) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.error(throwable);
    }
}
