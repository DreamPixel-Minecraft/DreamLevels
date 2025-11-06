package net.dreampixel.dreamlevels.command.sub.level.reward;

import lombok.var;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SenderType;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

import java.util.Collection;

@CommandInfo(
        name = "AutoReceive",
        aliases = "AutoClaim",
        permissions = "DreamLevels.Commands.AutoReceive",
        senderType = SenderType.PLAYER
)
public class AutoReceiveCommand extends SubCommand {
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var player = (Player) ctx.sender();
        var arguments = ctx.arguments();
        var rewardList = arguments[1].get(RewardList.class);

        var cnt = rewardList.autoReceive(player);
        if (cnt > 0) {
            LocaleUtils.sendCmdMessage(player, "reward.auto-receive",
                    "{amount}", String.valueOf(cnt));
        } else {
            LocaleUtils.sendCmdMessage(player, "reward.auto-receive-empty",
                    "{amount}", String.valueOf(cnt));
        }

        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        if (ctx.arguments().length == 2) {
            return RewardManager.getInstance().getRewardLists().keySet();
        }

        return TAB_EMPTY_LIST;
    }
}
