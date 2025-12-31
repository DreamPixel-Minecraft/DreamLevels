package net.dreampixel.dreamlevels.command.sub.levelspy;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SenderType;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

import java.util.Collection;

@CommandInfo(
        name = "LevelSpy",
        aliases = "ls",
        permissions = "DreamLevels.Commands.LevelSpy",
        senderType = SenderType.PLAYER
)
// dl ls [level]
public class LevelSpyCommand extends SubCommand {

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var sender = (Player) ctx.sender();

        // if the user has specified a level system
        if (ctx.hasArgument(1)) {
            var level = ctx.arguments()[1].get(Level.class);
            LevelSpyManager.getInstance().openLevelMenu(sender, level);
            return true;
        }

        LevelSpyManager.getInstance().openLevelOverallMenu(sender);
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        if (ctx.arguments().length == 2) {
            return LevelManager.getInstance().getLevels().keySet();
        }

        return TAB_EMPTY_LIST;
    }
}
