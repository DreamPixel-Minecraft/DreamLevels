package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import top.shadowpixel.shadowcore.util.logging.Logger;

@UtilityClass
public class Debugger {
    public static final String PREFIX = "[DL-DBG] ";
    private static final Logger logger = DreamLevels.getInstance().logger;

    public static void info(String... messages) {
        if (DreamLevels.getInstance().isDebugMode()) {
            for (var message : messages) {
                logger.info(PREFIX + message);
            }
        }
    }

    public static void warn(String... messages) {
        if (DreamLevels.getInstance().isDebugMode()) {
            for (var message : messages) {
                logger.warn(PREFIX + message);
            }
        }
    }

    public static void warn(String message, Throwable throwable) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.warn(PREFIX + message, throwable);
    }

    public static void warn(Throwable... throwable) {
        if (DreamLevels.getInstance().isDebugMode()) {
            logger.warn(PREFIX + " An error occurred. ");
            logger.warn(throwable);
        }
    }

    public static void error(String... messages) {
        if (DreamLevels.getInstance().isDebugMode()) {
            for (var message : messages) {
                logger.error(PREFIX + message);
            }
        }
    }

    public static void error(String message, Throwable throwable) {
        if (DreamLevels.getInstance().isDebugMode())
            logger.error(PREFIX + message, throwable);
    }

    public static void error(Throwable... throwable) {
        if (DreamLevels.getInstance().isDebugMode()) {
            logger.error("An error occurred. ");
            logger.error(throwable);
        }
    }
}
