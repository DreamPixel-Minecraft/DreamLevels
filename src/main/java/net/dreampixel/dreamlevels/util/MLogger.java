package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import net.dreampixel.dreamlevels.DreamLevels;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

/**
 * A simple message logger of DreamLevels, which reads messages from the Default Message Configuration.
 */
@UtilityClass
public class MLogger {
    private static final DreamLevels plugin = DreamLevels.getInstance();

    public static void info(String path) {
        Logger.info(getText(path));
    }

    public static void infoReplaced(String path, String... replacements) {
        Logger.info(ReplaceUtils.coloredReplace(getText(path), replacements));
    }

    public static void warn(String path) {
        Logger.warn(getText(path));
    }

    public static void warn(String path, Throwable throwable) {
        Logger.warn(getText(path), throwable);
    }

    public static void warnReplaced(String path, String... replacements) {
        Logger.warn(ReplaceUtils.coloredReplace(getText(path), replacements));
    }

    public static void error(String path) {
        Logger.error(getText(path));
    }

    public static void errorReplaced(String path, String... replacements) {
        Logger.error(ReplaceUtils.coloredReplace(getText(path), replacements));
    }

    public static void error(String path, Throwable throwable) {
        Logger.error(getText(path), throwable);
    }
    
    public static @NotNull String getText(String path) {
        return LocaleUtils.getMessage(path);
    }
}
