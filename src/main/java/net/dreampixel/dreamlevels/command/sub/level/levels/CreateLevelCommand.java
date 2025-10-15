package net.dreampixel.dreamlevels.command.sub.level.levels;

import lombok.var;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.util.MLogger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SenderType;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

@CommandInfo(
        name = "CreateLevel",
        aliases = "cl",
        senderType = SenderType.CONSOLE
)
public class CreateLevelCommand extends SubCommand {
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var arguments = ctx.arguments();
        var name = arguments[1].getString();
        var displayName = arguments[2].getString();
        if (LevelManager.getInstance().hasLevel(name)) {
            MLogger.info("level.create-duplicated");
            return true;
        }

        LevelManager.getInstance().create(name, displayName);
        return true;
    }
}
