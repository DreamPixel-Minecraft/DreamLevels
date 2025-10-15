package net.dreampixel.dreamlevels.command.sub.basic;

import lombok.var;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@CommandInfo(
        name = "Help"
)
public class HelpCommand extends SubCommand {
    protected String name = "help";

    public HelpCommand() {
        super();
    }

    public HelpCommand(String name) {
        super();
        this.name = name;
    }

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        var sender = ctx.sender();
        var label = ctx.label();
        var pages = getPages(sender);
        var page = "1";
        if (ctx.arguments().length > 1) {
            page = ctx.arguments()[1].getValue();
        }

        if (!pages.contains(page)) {
            page = "1";
        }

        SenderUtils.sendMessage(sender, ReplaceUtils.replace(getHelps(sender, page), "{cmd}", label,
                "{page}", page));
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext context) {
        return getPages(context.sender());
    }

    @SuppressWarnings("DataFlowIssue")
    private Set<String> getPages(CommandSender sender) {
        return LocaleUtils.getDefaultMessage().getConfigurationSection("command." + name).getKeys();
    }

    private List<String> getHelps(CommandSender sender, String page) {
        return LocaleUtils.getDefaultMessage().getStringList("command." + name + "." + page);
    }
}
