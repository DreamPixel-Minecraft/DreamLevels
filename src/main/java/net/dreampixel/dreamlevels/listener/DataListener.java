package net.dreampixel.dreamlevels.listener;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.util.entity.PlayerUtils;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;

public class DataListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onLogin(@NotNull AsyncPlayerPreLoginEvent event) {
        var uuid = event.getUniqueId();
        var dm = DataManager.getInstance();

        // check db connection
        if (dm.isDatabaseMode() && (dm.getDatabase() == null || !dm.getDatabase().isInitialized())) {
            var playerMsg = LocaleUtils.getMessage("data.database.disconnected");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, playerMsg);
            return;
        }

        dm.load(uuid, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();
        var dm = DataManager.getInstance();

        // check whether the data is loaded correctly, if not, kick the player.
        if (!dm.isLoaded(uuid)) {
            var kickMsg = LocaleUtils.getMessage("data.failure.player.failed-to-load");
            player.kickPlayer(kickMsg);

            // send feedback
            MLogger.errorReplaced("data.failure.admin.failed-to-load",
                    "{player}", uuid.toString());
            PlayerUtils.getOnlineOperators().forEach(p -> {
                var player_msg = LocaleUtils.getMessage(p, "data.failure.admin.failed-to-load",
                        "{player}", uuid.toString());
                SenderUtils.sendMessage(p, player_msg);
            });
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        // close and remove reward menus
        RewardManager.getInstance().removeRewardMenu(player);

        // unload the player's data, throw a notice to operators and the console if failed.
        var unloaded = DataManager.getInstance().unload(player.getUniqueId());
        if (!unloaded) {
            Bukkit.getScheduler().runTask(DreamLevels.getInstance(), () -> {
                var msg = LocaleUtils.getMessage(player, "data.failure.admin.failed-to-unload",
                        "{player}", player.getName());
                Logger.error(msg);
                PlayerUtils.getOnlineOperators().forEach(p -> SenderUtils.sendMessage(p, msg));
            });
        }
    }
}
