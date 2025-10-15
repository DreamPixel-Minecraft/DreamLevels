package net.dreampixel.dreamlevels.command.sub.level.reward;

import lombok.var;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SenderType;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CommandInfo(
        name = "Reward",
        senderType = SenderType.PLAYER
)
// /dl reward <reward>
public class RewardCommand extends SubCommand {
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var arguments = ctx.arguments();
        var reward = arguments[1].get(RewardList.class);
        if (!SenderUtils.hasPermissions(ctx.sender(), reward.getPermissions())) {
            LocaleUtils.sendCmdMessage(ctx.sender(), "reward.permissions-denied");
            return true;
        }

        reward.openRewardMenu((Player) ctx.sender());
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        if (ctx.arguments().length == 2) {
            return getOpenableRewards(ctx.sender());
        }

        return TAB_EMPTY_LIST;
    }

    public Set<String> getOpenableRewards(@NotNull CommandSender sender) {
        if (sender.isOp()) {
            return RewardManager.getInstance().getRewardLists().keySet();
        }

        return RewardManager.getInstance().getRewardLists().entrySet()
                .stream()
                .filter(t -> SenderUtils.hasPermissions(sender, t.getValue().getPermissions()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
