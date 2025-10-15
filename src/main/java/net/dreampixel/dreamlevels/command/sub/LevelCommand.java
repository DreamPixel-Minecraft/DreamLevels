package net.dreampixel.dreamlevels.command.sub;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.uid.UUIDStorage;
import top.shadowpixel.shadowcore.util.collection.ListUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// /dl <AddLevels> <players> <level> <amount> [feedback]
public abstract class LevelCommand extends SubCommand {
    public LevelCommand() {}

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var arguments = ctx.arguments();
        // basic variables
        var level = arguments[2].get(Level.class);
        var value = arguments[3].getDouble();

        // to check whether the player argument starts with "of:"
        // if so, run the offline modification
        var typedPlayer = arguments[1].getString();
        if (typedPlayer.startsWith("of:")) {
            if (Bukkit.getPlayer(typedPlayer.substring(3)) != null) {
                LocaleUtils.sendCmdMessage(ctx.sender(), "offline.online-detected");
                return true;
            }

            // execute offline commands and send feedback
            ctx.addProperty("player", typedPlayer.substring(3));
            executeOffline(ctx, level, value, (feedback) -> {
                if (feedback) {
                    if (arguments.length > 4 && arguments[4].getBoolean()) {
                        return;
                    }

                    sendOfflineFeedback(ctx, level, value);
                }
            });

            return true;
        }

        // add properties
        ctx.addProperty("players", arguments[1].getOnlinePlayers());
        var feedback = executeOnline(ctx, level, value);
        if (feedback) {
            if (arguments.length > 4 && !arguments[4].getBoolean()) {
                return true;
            }

            sendOnlineFeedback(ctx, level, value);
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
                return LevelManager.getInstance().getLevels().keySet();
            case 5:
                return ListUtils.asList("true", "false");
            default:
                return TAB_EMPTY_LIST;
        }
    }

    /**
     * Execute level-modifying commands for online players. <p>
     * <p>
     * For properties, we have:
     * <li> players - a list of players
     * <li> level   - level whose data should be modified
     * <li> amount  - the amount, which has a type of 'int' in levels, and 'double' in exp.
     *
     * @param ctx   Command context
     * @param level
     * @param value
     * @return Whether to send feedback message
     */
    public abstract boolean executeOnline(@NotNull CommandContext ctx, @NotNull Level level, double value);

    /**
     * This method will run to send success feedback to the command sender.
     *
     * @param ctx   Command context
     * @param level
     * @param value
     */
    public abstract void sendOnlineFeedback(@NotNull CommandContext ctx, @NotNull Level level, double value);

    /**
     * Execute level-modifying commands for offline players. <p>
     * <p>
     * For properties, we have:
     * <li> players - a list of player names
     * <li> level   - level whose data should be modified
     * <li> amount  - the amount, which has a type of 'int' in levels, and 'double' in exp.
     *
     * @param ctx        Command context
     * @param level
     * @param value
     * @param onFinished
     * @return Whether to send feedback message
     */
    public abstract void executeOffline(@NotNull CommandContext ctx, @NotNull Level level, double value, @NotNull Consumer<Boolean> onFinished);

    /**
     * This method will run to send all kinds of feedback to the command sender,
     * including success feedback, failed feedback, etc. <p>
     * <p>
     * Please notify that, the modification feedback will be saved as a property "feedback"
     *
     * @param ctx   Command context
     * @param level
     * @param value
     */
    public abstract void sendOfflineFeedback(@NotNull CommandContext ctx, @NotNull Level level, double value);
}
