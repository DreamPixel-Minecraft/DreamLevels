package net.dreampixel.dreamlevels.command.sub.level.edit.sub;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

@CommandInfo(
        name = "SetMaxLevels",
        permissions = "DreamLevels.Commands.Edit.SetMaxLevels"
)
public class SetMaxLevelsCommand extends SubCommand {
    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var level = ctx.arguments()[2].get(Level.class);
        var value = ctx.arguments()[3].getDouble();
        level.setDefaultRequiredExp(value);
        LocaleUtils.sendCmdMessage(ctx.sender(), "modify.set-max-levels",
                "{level}", level.getName(),
                "{value}", String.valueOf(value));
        return true;
    }
}
