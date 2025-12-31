package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.config.component.Configuration;
import top.shadowpixel.shadowcore.util.entity.PlayerUtils;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class LocaleUtils {

    @NotNull
    public static Configuration getDefaultMessage() {
        return DreamLevels.getInstance().getDefaultMessage();
    }

    @NotNull
    public static Configuration getDefaultEvents() {
        return DreamLevels.getInstance().getDefaultEvents();
    }

    @NotNull
    public static Configuration getLocaleMessage(@NotNull CommandSender sender) {
        if (DreamLevels.getInstance().getConfiguration().getBoolean("locale.use-default-locale")) {
            return getDefaultMessage();
        }

        if (sender instanceof Player) {
            var locale = LocaleManager.getInstance().getLocale(PlayerUtils.getLocale(((Player) sender)));
            if (locale == null) {
                return getDefaultMessage();
            }

            return Objects.requireNonNull(locale.getConfig("Message"),
                    String.format("locale of player %s is null", sender.getName()));
        }

        return getDefaultMessage();
    }

    @NotNull
    public static Configuration getLocaleEvents(@NotNull CommandSender sender) {
        if (DreamLevels.getInstance().getConfiguration().getBoolean("locale.use-default-locale")) {
            return getDefaultEvents();
        }

        if (sender instanceof Player) {
            var locale = LocaleManager.getInstance().getLocale(PlayerUtils.getLocale(((Player) sender)));
            if (locale == null) {
                return getDefaultEvents();
            }

            return Objects.requireNonNull(locale.getConfig("Events"),
                    String.format("locale of player %s is null", sender.getName()));
        }

        return getDefaultEvents();
    }

    @NotNull
    public static String getMessage(String path, String... replacements) {
        return ReplaceUtils.coloredReplace(
                getDefaultMessage().getString(path, "undefined: " + path).replace("{prefix}", DreamLevels.getPrefix()), replacements);
    }

    @NotNull
    public static String getMessage(CommandSender sender, String path, String... replacements) {
        return ReplaceUtils.coloredReplace(
                getLocaleMessage(sender).getString(path, getDefaultMessage().getString(path, "undefined: " + path)).replace("{prefix}", DreamLevels.getPrefix()), sender, replacements
        );
    }

    @NotNull
    public static List<String> getMessages(CommandSender sender, String path, String... replacements) {
        return ReplaceUtils.coloredReplace(
                getLocaleMessage(sender).getStringList(path), sender, replacements
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
