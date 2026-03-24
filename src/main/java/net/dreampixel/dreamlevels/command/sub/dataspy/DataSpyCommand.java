package net.dreampixel.dreamlevels.command.sub.dataspy;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SenderType;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;

import java.util.Collection;

@CommandInfo(
        name = "DataSpy",
        aliases = "ds",
        permissions = "DreamLevels.Commands.DataSpy",
        senderType = SenderType.PLAYER
)
// /dl ds [player] [level]
public class DataSpyCommand extends SubCommand {

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var sender = (Player) ctx.sender();
        var arguments = ctx.arguments();

        SenderUtils.sendMessage(sender, "&f&lWelcome to use &5&lDataSpy&f&l.");

        // the user has specificed a player
        if (ctx.hasArgument(1)) {
            var player = arguments[1].getOnlinePlayer();

            // the user has specified a level
            if (ctx.hasArgument(2)) {
                // open level data menu
                var level = arguments[2].get(Level.class);
                DataSpyManager.getInstance().openLevelDataMenu(sender, player.getUniqueId(), level);
                return true;
            }

            // open overall level data menu
            DataSpyManager.getInstance().openLevelDataOverallMenu(sender, player.getUniqueId());
            return true;
        }

        // open player data menu
        DataSpyManager.getInstance().openPlayerDataMenu(sender);
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        switch (ctx.arguments().length) {
            case 2:
                return TAB_ONLINE_PLAYERS_LIST;
            case 3:
                return LevelManager.getInstance().getLevels().keySet();
            default:
                return TAB_EMPTY_LIST;
        }
    }
}
