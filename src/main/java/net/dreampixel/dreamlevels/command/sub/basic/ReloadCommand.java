package net.dreampixel.dreamlevels.command.sub.basic;

import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.config.ConfigManager;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.CommandContext;
import top.shadowpixel.shadowcore.api.command.SubCommand;
import top.shadowpixel.shadowcore.api.command.annotation.CommandInfo;
import top.shadowpixel.shadowcore.util.collection.ListUtils;
import top.shadowpixel.shadowcore.util.plugin.ManagerUtils;

import java.util.Collection;

@CommandInfo(
        name = "Reload",
        aliases = {"rl"},
        permissions = "DreamLevels.Command.Reload"
)
public class ReloadCommand extends SubCommand {

    @Override
    public boolean execute(@NotNull CommandContext ctx) {
        if (ctx.arguments().length > 1) {
            switch (ctx.arguments()[1].getValue().toLowerCase()) {
                case "config":
                    ConfigManager.getInstance().reload();
                    break;
                case "locale":
                    LocaleManager.getInstance().reload();
                    break;
                case "level":
                    LevelManager.getInstance().reload();
                    break;
                case "reward":
                    RewardManager.getInstance().reload();
                    break;
                case "data":
                    DataManager.getInstance().reload();
                    break;
                case "sync":
                    DreamLevels.getInstance().initSyncService();
                    break;
            }
        } else {
            ManagerUtils.reloadManagers(
                    ConfigManager.getInstance(),
                    LocaleManager.getInstance(),
                    LevelManager.getInstance(),
                    RewardManager.getInstance(),
                    DataManager.getInstance()
            );

            // sync service
            DreamLevels.getInstance().initSyncService();
        }

        LocaleUtils.sendCmdMessage(ctx.sender(), "generic.reloaded");
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext context) {
        if (context.arguments().length == 2) {
            return ListUtils.asList("Config", "Locale", "Data", "Sync", "Level", "Reward");
        }

        return null;
    }
}
