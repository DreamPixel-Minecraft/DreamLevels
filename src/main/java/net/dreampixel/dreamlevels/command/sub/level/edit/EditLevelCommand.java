package net.dreampixel.dreamlevels.command.sub.level.edit;

import lombok.var;
import net.dreampixel.dreamlevels.command.sub.level.edit.sub.SetDefaultLevelsCommand;
import net.dreampixel.dreamlevels.command.sub.level.edit.sub.SetDisplayNameCommand;
import net.dreampixel.dreamlevels.command.sub.level.edit.sub.SetMaxLevelsCommand;
import net.dreampixel.dreamlevels.command.sub.level.edit.sub.SetRequiredExpCommand;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

import java.util.Collection;

@CommandInfo(
        name = "EditLevel",
        permissions = "DreamLevels.Commands.EditLevel"
)
public class EditLevelCommand extends SubCommand {

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var sender = ctx.sender();
        if (ctx.arguments().length <= 2) {
            LocaleUtils.sendCmdMessages(sender, "admin.edit-level",
                    "{cmd}", ctx.label());
            return true;
        }

        return false;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext ctx) {
        switch (ctx.arguments().length) {
            case 2:
                return TAB_SUB_COMMANDS_LIST;
            case 3:
                return LevelManager.getInstance().getLevels().keySet();
            default:
                return TAB_EMPTY_LIST;
        }
    }

    @Override
    public void initialize() {
        addSubCommand(
                new SetDefaultLevelsCommand(),
                new SetRequiredExpCommand(),
                new SetDisplayNameCommand(),
                new SetMaxLevelsCommand()
        );
    }
}
