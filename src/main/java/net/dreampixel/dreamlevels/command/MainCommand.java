package net.dreampixel.dreamlevels.command;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.command.sub.basic.AdminCommand;
import net.dreampixel.dreamlevels.command.sub.basic.HelpCommand;
import net.dreampixel.dreamlevels.command.sub.basic.ReloadCommand;
import net.dreampixel.dreamlevels.command.sub.level.ResetCommand;
import net.dreampixel.dreamlevels.command.sub.level.SetMultipleCommand;
import net.dreampixel.dreamlevels.command.sub.level.exp.AddExpCommand;
import net.dreampixel.dreamlevels.command.sub.level.exp.SetExpCommand;
import net.dreampixel.dreamlevels.command.sub.level.levels.*;
import net.dreampixel.dreamlevels.command.sub.level.exp.RemoveExpCommand;
import net.dreampixel.dreamlevels.command.sub.level.reward.CreateRewardCommand;
import net.dreampixel.dreamlevels.command.sub.level.reward.OpenRewardCommand;
import net.dreampixel.dreamlevels.command.sub.level.reward.RewardCommand;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.api.command.exception.MissingPropertyException;
import top.shadowpixel.shadowcore.api.command.exception.ParameterizedCommandInterruptedException;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;
import top.shadowpixel.shadowcore.util.object.NumberUtils;

import java.util.Collection;

@CommandInfo(
        name = "DreamLevels"
)
public class MainCommand extends SubCommand {
    @Override
    public void initialize() {
        addSubCommand(
                new HelpCommand(),
                new AdminCommand(),
                new ReloadCommand(),
                // level
                new AddLevelsCommand(),
                new CreateLevelCommand(),
                new RemoveLevelsCommand(),
                new SetLevelsCommand(),
                // exp
                new AddExpCommand(),
                new RemoveExpCommand(),
                new SetExpCommand(),
                // reward
                new CreateRewardCommand(),
                new OpenRewardCommand(),
                new RewardCommand(),
                // others
                new ResetCommand(),
                new SetMultipleCommand()
        );

        // message exception handlers
        addExceptionHandler("unknown cmd", "command not found",
                ctx -> LocaleUtils.sendCmdMessage(ctx.sender(), "generic.unknown-command"));
        addExceptionHandler("not player", "not player",
                ctx -> LocaleUtils.sendCmdMessage(ctx.sender(), "generic.only-for-player"));
        addExceptionHandler("only console", "not console",
                ctx -> LocaleUtils.sendCmdMessage(ctx.sender(), "generic.only-for-console"));
        addExceptionHandler("no permissions", "no permissions",
                ctx -> LocaleUtils.sendCmdMessage(ctx.sender(), "generic.no-permissions"));
        addExceptionHandler("no property", t -> t instanceof MissingPropertyException, ctx -> {
            LocaleUtils.sendCmdMessage(ctx.sender(), "generic.property-required",
                    "{property}", ctx.exception().getMessage());
            return true;
        });

        // exception-specified handlers
        addExceptionHandler("no argument", t -> t instanceof IndexOutOfBoundsException,
                ctx -> {
                    // find index
                    var num = ctx.exception().getMessage();
                    if (num.toLowerCase().startsWith("index")) {
                        num = num.split(" ")[1];
                    }

                    // +1
                    num = String.valueOf(NumberUtils.parseInt(num, 0) + 1);
                    // send message
                    LocaleUtils.sendCmdMessage(ctx.sender(),
                            "generic.parameters-error",
                            "{index}", num);
                    return true;
                });

        addExceptionHandler("parameter error", t -> t instanceof ParameterizedCommandInterruptedException, ctx -> {
            var exc = (ParameterizedCommandInterruptedException) ctx.exception();
            var argument = exc.getArgument();
            switch (exc.getMessage()) {
                case "not int":
                    LocaleUtils.sendCmdMessage(ctx.sender(), "generic.not-an-integer",
                            "{index}", String.valueOf(argument.getIndex() + 1));
                    return true;
                case "not double":
                    LocaleUtils.sendCmdMessage(ctx.sender(), "generic.not-an-number",
                            "{index}", String.valueOf(argument.getIndex() + 1));
                    return true;
                case "player not found":
                    LocaleUtils.sendCmdMessage(ctx.sender(), "generic.unknown-player",
                            "{index}", String.valueOf(argument.getIndex() + 1),
                            "{player}", argument.getString());
                    return true;
                case "level not found":
                    LocaleUtils.sendCmdMessage(ctx.sender(), "generic.unknown-level",
                            "{index}", String.valueOf(argument.getIndex() + 1),
                            "{level}", argument.getString());
                    return true;
                case "reward not found":
                    LocaleUtils.sendCmdMessage(ctx.sender(), "generic.unknown-reward",
                            "{index}", String.valueOf(argument.getIndex() + 1),
                            "{reward}", argument.getString());
                    return true;
            }

            return false;
        });
    }

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        if (ctx.arguments().length > 0) {
            return false; // return false to run subcommands.
        }

        // send plugin info
        var sender = ctx.sender();
        SenderUtils.sendMessage(sender, LocaleUtils.getMessage(sender, "startup.info"),
                "{cmd}", ctx.label(),
                "{version}", DreamLevels.getVersion());
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext context) {
        if (context.arguments().length > 1) {
            return TAB_RUN_SUBCOMMAND;
        }

        // all available subcommands
        return TAB_SUB_COMMANDS_LIST;
    }
}
