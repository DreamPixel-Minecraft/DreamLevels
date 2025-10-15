package net.dreampixel.dreamlevels.util;


import lombok.experimental.UtilityClass;
import net.dreampixel.dreamlevels.DreamLevels;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

/**
 * A simple logger wrapper of DreamGameEngine
 */
@UtilityClass
public class Logger {
    private static final top.shadowpixel.shadowcore.util.logging.Logger logger = DreamLevels.getInstance().logger;

    public static void info(String... messages) {
        logger.info(messages);
    }

    public static void infoReplaced(@NotNull String text, @NotNull String... replacements) {
        logger.info(ReplaceUtils.replace(text, replacements));
    }

    public static void warn(String... messages) {
        logger.warn(messages);
    }

    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void warnReplaced(@NotNull String text, @NotNull String... replacements) {
        logger.warn(ReplaceUtils.replace(text, replacements));
    }

    public static void warn(Throwable... throwable) {
        logger.warn(throwable);
    }

    public static void error(String... messages) {
        logger.error(messages);
    }

    public static void errorReplaced(@NotNull String text, @NotNull String... replacements) {
        logger.error(ReplaceUtils.replace(text, replacements));
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void error(Throwable... throwable) {
        logger.error(throwable);
    }
}
