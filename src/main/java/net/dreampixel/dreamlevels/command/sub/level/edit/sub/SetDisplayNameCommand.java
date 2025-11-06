package net.dreampixel.dreamlevels.command.sub.level.edit.sub;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

@CommandInfo(
        name = "SetDisplayName",
        permissions = "DreamLevels.Commands.Edit.SetDisplayName"
)
public class SetDisplayNameCommand extends SubCommand {
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var level = ctx.arguments()[2].get(Level.class);
        var value = ctx.arguments()[3].getString();
        level.setDisplayName(value);
        LocaleUtils.sendCmdMessage(ctx.sender(), "modify.set-display-name",
                "{level}", level.getName(),
                "{value}", value);
        return true;
    }
}
