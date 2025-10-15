package net.dreampixel.dreamlevels.command.sub.level.levels;

import lombok.var;
import net.dreampixel.dreamlevels.command.sub.LevelCommand;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.api.plugin.PlayerSet;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
@CommandInfo(
        name = "RemoveLevels",
        permissions = "DreamLevels.Commands.RemoveLevels"
)
public class RemoveLevelsCommand extends LevelCommand {
    @Override
    public boolean executeOnline(@NotNull CommandContext ctx, @NotNull Level level, double value) {
        var players = (PlayerSet) ctx.getProperty("players");
        players.forEach(p -> level.getLevelData(p).removeLevels((int) value, true));
        return true;
    }

    @Override
    public void sendOnlineFeedback(@NotNull CommandContext ctx, @NotNull Level level, double value) {
        var players = (PlayerSet) ctx.getProperty("players");
        LocaleUtils.sendCmdMessage(ctx.sender(), "level.remove-levels",
                "{player}", players.getName(),
                "{level}", level.getName(),
                "{level-displayName}", level.getDisplayName(),
                "{amount}", String.valueOf((int) value));
    }

    @Override
    public void executeOffline(@NotNull CommandContext ctx, @NotNull Level level, double value, @NotNull Consumer<Boolean> onFinished) {
        var player = (String) ctx.getProperty("player");

        // send sync-mode notification
        LocaleUtils.sendCmdMessage(ctx.sender(), "offline.sync-mode");

        // handle offline data for player
        DataManager.getInstance()
                .getOfflineLevelDataModifier(
                        player,
                        level.getName(),
                        feedback -> {
                            ctx.addProperty("feedback", feedback);
                            onFinished.accept(true);
                        }
                )
                .removeLevels((int) value);
    }

    @Override
    public void sendOfflineFeedback(@NotNull CommandContext ctx, @NotNull Level level, double value) {
        var feedback = (String) ctx.getProperty("feedback");
        var player = (String) ctx.getProperty("player");

        // handle success feedback
        if (feedback.startsWith("ok")) {
            if (feedback.equals("ok offline")) {
                LocaleUtils.sendCmdMessage(ctx.sender(), "offline.offline-success");
            } else {
                LocaleUtils.sendCmdMessage(ctx.sender(), "offline.sync-success");
            }

            LocaleUtils.sendCmdMessage(ctx.sender(), "level.remove-levels",
                    "{player}", player,
                    "{level}", level.getName(),
                    "{level-displayName}", level.getDisplayName(),
                    "{amount}", String.valueOf(value));
        } else {
            // handle failure
            LocaleUtils.sendOfflineFailure(ctx, feedback);
        }
    }
}
