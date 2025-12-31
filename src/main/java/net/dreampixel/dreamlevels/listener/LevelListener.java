package net.dreampixel.dreamlevels.listener;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.api.event.*;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.util.object.NumberUtils;

public class LevelListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLevelsModified(PlayerLevelsModifiedEvent event) {
        var level = event.getLevel();
        var player = event.getPlayer();

        var ee = (ExecutableEvent) null;
        switch (event.getModificationType()) {
            case ADD:
                ee = level.getLevelEvent(player, "levels-added");
                break;
            case REMOVE:
                ee = level.getLevelEvent(player, "levels-removed");
                break;
            case SET:
                ee = level.getLevelEvent(player, "levels-set");
                break;
        }

        if (ee != null) {
            ee.replace("{levels}", String.valueOf(event.getAmount()));
            ee.replace("{current-levels}", String.valueOf(level.getLevelData(player).getLevels()));
            ee.execute(DreamLevels.getInstance(), player);
        }

        // refresh reward menu items
        RewardManager.getInstance().updateRewardMenus(player, level);

        // check leveling up
        level.getLevelData(player).checkLevelUp();

        // save data
        DataManager.getInstance().getPlayerData(player).saveAsync();

        // update data spy menus
        DataSpyManager.getInstance().updateMenu(event.getPlayer(), event.getLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerExpModified(PlayerExpModifiedEvent event) {
        var level = event.getLevel();
        var player = event.getPlayer();
        var data = level.getLevelData(player);

        var ee = (ExecutableEvent) null;
        switch (event.getModificationType()) {
            case ADD:
                ee = level.getLevelEvent(player, "exp-received");
                if (ee != null) {
                    ee.replace("{final-exp}", String.valueOf(data.getMultiple() * event.getAmount()));
                }
                break;
            case REMOVE:
                ee = level.getLevelEvent(player, "exp-removed");
                break;
            case SET:
                ee = level.getLevelEvent(player, "exp-set");
                break;
        }

        if (ee != null) {
            ee.replace("{exp}", String.valueOf(event.getAmount()));
            ee.execute(DreamLevels.getInstance(), player);
        }

        // check leveling up
        data.checkLevelUp();

        // save data
        DataManager.getInstance().getPlayerData(player).saveAsync();

        // update data spy menus
        DataSpyManager.getInstance().updateMenu(event.getPlayer(), event.getLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMultipleModified(PlayerMultipleModifiedEvent event) {
        var ee = event.getLevel().getLevelEvent(event.getPlayer(), "multiple-set");
        if (ee != null) {
            ee.replace("{multiple}", NumberUtils.cutString(event.getAmount(), 2));
            ee.execute(DreamLevels.getInstance(), event.getPlayer());
        }

        // save data
        DataManager.getInstance().getPlayerData(event.getPlayer()).saveAsync();

        // update data spy menus
        DataSpyManager.getInstance().updateMenu(event.getPlayer(), event.getLevel());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDataReset(PlayerDataResetEvent event) {
        var ee = event.getLevel().getLevelEvent(event.getPlayer(), "reset");
        if (ee != null) {
            ee.execute(DreamLevels.getInstance(), event.getPlayer());
        }

        // save data
        DataManager.getInstance().getPlayerData(event.getPlayer()).saveAsync();

        // update experience bar
        LevelManager.getInstance().updateExperienceBar(event.getPlayer());

        // update data spy menus
        DataSpyManager.getInstance().updateMenu(event.getPlayer(), event.getLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDataResetAll(PlayerDataResetAllEvent event) {
        var ee = (ExecutableEvent) LocaleUtils.getLocaleEvents(event.getPlayer()).get("reset-all-events");
        if (ee != null) {
            ee.execute(DreamLevels.getInstance(), event.getPlayer());
        }

        // save data
        DataManager.getInstance().getPlayerData(event.getPlayer()).saveAsync();

        // update experience bar
        LevelManager.getInstance().updateExperienceBar(event.getPlayer());

        // update data spy menus
        DataSpyManager.getInstance().updateMenus(event.getPlayer());
    }
}
