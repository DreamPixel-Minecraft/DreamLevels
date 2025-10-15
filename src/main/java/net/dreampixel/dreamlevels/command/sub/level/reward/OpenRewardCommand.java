package net.dreampixel.dreamlevels.command.sub.level.reward;

import lombok.var;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

import java.util.Collection;

@CommandInfo(
        name = "OpenReward",
        aliases = "or",
        permissions = "DreamLevels.Commands.OpenReward"
)
// /dl OpenReward <players> <rewards>
public class OpenRewardCommand extends SubCommand {

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var arguments = ctx.arguments();
        var players = arguments[1].getOnlinePlayers();
        var reward = arguments[2].get(RewardList.class);
        players.forEach(reward::openRewardMenu);
        LocaleUtils.sendCmdMessage(ctx.sender(), "reward.open-menu-others",
                "{player}", players.getName());
        return super.execute(ctx);
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        switch (ctx.arguments().length) {
            case 2:
                return TAB_ONLINE_PLAYERS_SET;
            case 3:
                return RewardManager.getInstance().getRewardLists().keySet();
            default:
                return TAB_EMPTY_LIST;
        }
    }
}
