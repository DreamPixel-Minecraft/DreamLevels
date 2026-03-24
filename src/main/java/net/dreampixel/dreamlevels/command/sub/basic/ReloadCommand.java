package net.dreampixel.dreamlevels.command.sub.basic;

import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.config.ConfigManager;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
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
        // reload the specific section
        if (ctx.arguments().length > 1) {
            switch (ctx.arguments()[1].getValue().toLowerCase()) {
                // reload config manager
                case "config":
                    ConfigManager.getInstance().reload();
                    break;
                // reload locale manager
                case "locale":
                    LocaleManager.getInstance().reload();
                    break;
                // reload level manager
                case "level":
                    LevelManager.getInstance().reload();
                    break;
                // reload reward manager
                case "reward":
                    RewardManager.getInstance().reload();
                    break;
                // reload data manager
                case "data":
                    DataManager.getInstance().reload();
                    break;
                // reload sync service
                case "sync":
                    DreamLevels.getInstance().initSyncService();
                    break;
                // reload dataspy manager
                case "dataspy":
                    DreamLevels.getInstance().getDataSpyManager().reload();
                    break;
                // reload the life cycle task
                case "lifecycletask":
                    DreamLevels.getInstance().startLifeCycleTask(false);
                    break;
            }
        } else {
            // reload all managers
            ManagerUtils.reloadManagers(
                    ConfigManager.getInstance(),
                    LocaleManager.getInstance(),
                    LevelManager.getInstance(),
                    RewardManager.getInstance(),
                    DataManager.getInstance(),
                    DataSpyManager.getInstance(),
                    LevelSpyManager.getInstance()
            );

            // reload life cycle task
            DreamLevels.getInstance().startLifeCycleTask(false);

            // reload sync service
            DreamLevels.getInstance().initSyncService();
        }

        LocaleUtils.sendCmdMessage(ctx.sender(), "generic.reloaded");
        return true;
    }

    @Override
    public @Nullable Collection<String> complete(@NotNull CommandContext context) {
        if (context.arguments().length == 2) {
            return ListUtils.asList("Config", "Locale", "Data", "Sync", "Level", "Reward", "LifeCycleTask");
        }

        return null;
    }
}
