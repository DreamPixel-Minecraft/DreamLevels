package net.dreampixel.dreamlevels.command.sub.level.reward;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.MLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SenderType;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

import java.util.Collection;
import java.util.Collections;

@CommandInfo(
        name = "CreateReward",
        aliases = "cr",
        senderType = SenderType.CONSOLE
)
public class CreateRewardCommand extends SubCommand {
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var arguments = ctx.arguments();
        var name = arguments[1].getString();
        var level = arguments[2].get(Level.class);
        if (RewardManager.getInstance().hasRewardList(name)) {
            MLogger.info("reward.create-duplicated");
            return true;
        }

        RewardManager.getInstance().create(name, level.getName());
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        // /dl cr <name> <level>
        if (ctx.arguments().length == 2) {
            return LevelManager.getInstance().getLevels().keySet();
        }

        return Collections.emptyList();
    }
}
