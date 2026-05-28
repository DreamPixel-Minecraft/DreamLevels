package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.config.component.Configuration;
import top.shadowpixel.shadowcore.api.locale.Locale;
import top.shadowpixel.shadowcore.util.entity.PlayerUtils;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class LocaleUtils {
    /**
     * Get a locale with a specific name. Please note that this method won't forcibly
     * return the default locale.
     *
     * @param locale Locale name
     * @return Locale
     */
    public static @Nullable Locale getLocale(@NotNull String locale) {
        return LocaleManager.getInstance().getLocale(locale);
    }

    /**
     * @return Whether the default is forcibly used
     */
    public static boolean isDefaultLocaleUsed() {
        return DreamLevels.getInstance().getConfiguration().getBoolean("locale.use-default-locale");
    }

    /**
     * @return Default locale
     */
    @NotNull
    public static Locale getDefaultLocale() {
        return LocaleManager.getInstance().getDefaultLocale();
    }

    /**
     * @return Default message configuration
     */
    @NotNull
    public static Configuration getDefaultMessage() {
        return DreamLevels.getInstance().getDefaultMessage();
    }

    /**
     * @return Default event configuration
     */
    @NotNull
    public static Configuration getDefaultEvents() {
        return DreamLevels.getInstance().getDefaultEvents();
    }

    /**
     * Get the locale this command sender is using. If the command sender is the console,
     * then return the default locale; if the sender is a player, return the locale of the
     * player's game language. <p>
     * Return the default locale if it's forcibly used. ("locale.use-default-locale" in the config)
     *
     * @param sender Command sender
     * @return Locale
     */
    @NotNull
    public static Locale getLocale(@NotNull CommandSender sender) {
        if (isDefaultLocaleUsed()) {
            return getDefaultLocale();
        }

        if (sender instanceof Player) {
            var playerLocale = PlayerUtils.getLocale(((Player) sender));
            var locale = getLocale(playerLocale);
            if (locale != null) {
                return locale;
            }
        }

        return getDefaultLocale();
    }

    /**
     * Get the locale this player is using. return the locale of the player's game language. <p>
     * Return null if the locale is missing, return the default locale if it's forcibly used
     * (enabled "locale.use-default-locale" in the config).
     *
     * @param player Command sender
     * @return Locale
     */
    public static @Nullable Locale getNullableLocale(Player player) {
        if (isDefaultLocaleUsed()) {
            return getDefaultLocale();
        }

        if (player != null) {
            var playerLocale = PlayerUtils.getLocale(((Player) player));
            return getLocale(playerLocale);
        }

        return null;
    }

    @NotNull
    public static Configuration getLocaleMessage(@NotNull CommandSender sender) {
        return Objects.requireNonNull(getLocale(sender).getConfig("Message"),
                String.format("Message configuration of player %s is null", sender.getName()));
    }

    @NotNull
    public static Configuration getLocaleEvents(@NotNull CommandSender sender) {
        return Objects.requireNonNull(getLocale(sender).getConfig("Events"),
                String.format("Events configuration of player %s is null", sender.getName()));
    }

    @NotNull
    public static String getMessage(String path, String... replacements) {
        return ReplaceUtils.coloredReplace(getDefaultMessage().getString(path, "undefined: " + path)
                        .replace("{prefix}", DreamLevels.getPrefix()), replacements);
    }

    @NotNull
    public static String getMessage(CommandSender sender, String path, String... replacements) {
        return ReplaceUtils.coloredReplace(getLocaleMessage(sender)
                .getString(path, getDefaultMessage().getString(path, "undefined: " + path))
                .replace("{prefix}", DreamLevels.getPrefix()), sender, replacements
        );
    }

    @NotNull
    public static List<String> getMessages(CommandSender sender, String path, String... replacements) {
        return ReplaceUtils.coloredReplace(getLocaleMessage(sender).getStringList(path), sender, replacements
        );
    }

    @NotNull
    public static String getCmdMessage(CommandSender sender, String path, String... replacements) {
        return getMessage(sender, "command." + path, replacements);
    }

    @NotNull
    public static List<String> getCmdMessages(CommandSender sender, String path, String... replacements) {
        return getMessages(sender, "command." + path, replacements);
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String path, @NotNull String... replacements) {
        sender.sendMessage(getMessage(sender, path, replacements));
    }

    public static void sendMessages(@NotNull CommandSender sender, @NotNull String path, @NotNull String... replacements) {
        SenderUtils.sendMessage(sender, getMessages(sender, path, replacements));
    }

    public static void sendCmdMessage(@NotNull CommandSender sender, @NotNull String path, @NotNull String... replacements) {
        sender.sendMessage(getCmdMessage(sender, path, replacements));
    }

    public static void sendCmdMessages(@NotNull CommandSender sender, @NotNull String path, @NotNull String... replacements) {
        SenderUtils.sendMessage(sender, getCmdMessages(sender, path, replacements));
    }

    public static void sendOfflineFailure(@NotNull CommandContext ctx, @NotNull String result) {
        var sender = ctx.sender();
        var path = "offline.unknown-error";
        switch (result) {
            case "unknown player":
                path = "offline.unknown-data";
                break;
            case "unknown level":
                path = "offline.unknown-level";
                break;
            case "unknown level data":
                path = "offline.unknown-level-data";
                break;
            case "timed out":
                path = "offline.sync-timed-out";
                break;
            case "unknown server":
                path = "offline.inaccessible-server";
                break;
        }

        sendCmdMessage(sender, path,
                "{error}", result);
    }
}
