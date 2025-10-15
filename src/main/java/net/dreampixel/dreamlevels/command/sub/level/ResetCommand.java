package net.dreampixel.dreamlevels.command.sub.level;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.api.uid.UUIDStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@CommandInfo(
        name = "Reset",
        permissions = "DreamLevels.Commands.Reset"
)
// /dl reset <player> <level> [notification]
public class ResetCommand extends SubCommand {

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var sender = ctx.sender();
        var arguments = ctx.arguments();
        var levelStr = arguments[2].getString();

        // to check whether the player argument starts with "of:"
        // if so, run the offline modification
        var typedPlayer = arguments[1].getString();
        if (typedPlayer.startsWith("of:")) {
            if (Bukkit.getPlayer(typedPlayer.substring(3)) != null) {
                LocaleUtils.sendCmdMessage(ctx.sender(), "offline.online-detected");
                return true;
            }

            // execute offline commands and send feedback
            LocaleUtils.sendCmdMessage(ctx.sender(), "offline.sync-mode");

            // add properties
            ctx.addProperty("player", typedPlayer);
            ctx.addProperty("level", levelStr);

            // handle offline data for player
            if (levelStr.equalsIgnoreCase("*")) {
                // reset all
                DataManager.getInstance().getOfflineDataModifier(
                                typedPlayer.substring(3),
                                feedback -> {
                                    // send feedback
                                    if (arguments.length > 3 && !arguments[3].getBoolean()) {
                                        return;
                                    }

                                    sendOfflineFeedback(ctx, feedback);
                                }
                        )
                        .resetAll();
            } else {
                // reset a specific level
                DataManager.getInstance()
                        .getOfflineLevelDataModifier(
                                typedPlayer.substring(3),
                                levelStr,
                                feedback -> {
                                    // send feedback
                                    if (arguments.length > 3 && !arguments[3].getBoolean()) {
                                        return;
                                    }

                                    sendOfflineFeedback(ctx, feedback);
                                }
                        )
                        .reset();
            }

            return true;
        }

        var players = arguments[1].getOnlinePlayers();
        if (levelStr.equals("*")) {
            // for all levels
            players.forEach(LevelManager.getInstance()::resetAll);

            // send feedback
            if (arguments.length > 3 && !arguments[3].getBoolean()) {
                return true;
            }

            LocaleUtils.sendCmdMessage(sender, "level.reset-all",
                    "{player}", players.getName());
        } else {
            // for a specific level
            var level = arguments[2].get(Level.class);
            players.forEach(p -> {
                var data = level.getLevelData(p);
                data.reset();
            });

            // send feedback
            if (arguments.length > 3 && !arguments[3].getBoolean()) {
                return true;
            }

            LocaleUtils.sendCmdMessage(sender, "level.reset",
                    "{player}", players.getName(),
                    "{level}", level.getName(),
                    "{level-displayName}", level.getDisplayName());
        }

        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        switch (ctx.arguments().length) {
            case 2:
                // completion for offline players
                // e.g. typed "of:" -> "of:player1"
                var typed = ctx.arguments()[1].getValue();
                if (typed.startsWith("of:")) {
                    var players = new HashSet<>(UUIDStorage.getStoredPlayers());
                    return players.stream()
                            .map(t -> "of:" + t)
                            .collect(Collectors.toList());
                }

                return tabListBuilder(TAB_ONLINE_PLAYERS_SET)
                        .add(0, "of:")
                        .build();
            case 3:
                return tabListBuilder("*")
                        .append(LevelManager.getInstance().getLevels().keySet())
                        .build();
            default:
                return TAB_EMPTY_LIST;
        }
    }

    public void sendOfflineFeedback( CommandContext ctx, String feedback) {
        var player = (String) ctx.getProperty("player");
        var level = (String) ctx.getProperty("level");

        // handle success feedback
        if (feedback.startsWith("ok")) {
            if (feedback.equals("ok offline")) {
                LocaleUtils.sendCmdMessage(ctx.sender(), "offline.offline-success");
            } else {
                LocaleUtils.sendCmdMessage(ctx.sender(), "offline.sync-success");
            }

            LocaleUtils.sendCmdMessage(ctx.sender(), level.equals("*") ? "level.reset-all"  : "level.reset",
                    "{player}", player,
                    "{level}", level,
                    "{level-displayName}", level);
        } else {
            // handle failure
            LocaleUtils.sendOfflineFailure(ctx, feedback);
        }
    }
}
