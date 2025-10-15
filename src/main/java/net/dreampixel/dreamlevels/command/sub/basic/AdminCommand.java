package net.dreampixel.dreamlevels.command.sub.basic;

import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;

@CommandInfo(
        name = "Admin",
        permissions = "DreamLevels.Commands.Admin"
)
public class AdminCommand extends HelpCommand {
    public AdminCommand() {
        super("admin");
    }
}
