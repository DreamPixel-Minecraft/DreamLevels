package net.dreampixel.dreamlevels.listener;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.api.event.*;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.level.LevelEventContainer;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
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

        // execute level events
        var defaultEvent = (ExecutableEvent) null;
        var localizedEvent = (ExecutableEvent) null;
        switch (event.getModificationType()) {
            case ADD:
                var currentLevel = level.getLevelData(player).getLevels();
                defaultEvent = level.getDefaultEvent(c -> c.getAddLevelEvent(currentLevel));
                localizedEvent = level.getLocalizedEvent(player, c -> c.getAddLevelEvent(currentLevel));
                break;
            case REMOVE:
                defaultEvent = level.getDefaultEvent(LevelEventContainer::getLevelsRemovedEvent);
                localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getLevelsRemovedEvent);
                break;
            case SET:
                defaultEvent = level.getDefaultEvent(LevelEventContainer::getLevelsSetEvent);
                localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getLevelsSetEvent);
                break;
        }

        if (defaultEvent != null) {
            defaultEvent.replace("{levels}", String.valueOf(event.getAmount()));
            defaultEvent.replace("{current-levels}", String.valueOf(level.getLevelData(player).getLevels()));
            defaultEvent.execute(DreamLevels.getInstance(), player);
        }

        if (localizedEvent != null) {
            localizedEvent.replace("{levels}", String.valueOf(event.getAmount()));
            localizedEvent.replace("{current-levels}", String.valueOf(level.getLevelData(player).getLevels()));
            localizedEvent.execute(DreamLevels.getInstance(), player);
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

        // execute localized level event
        var defaultEvent = (ExecutableEvent) null;
        var localizedEvent = (ExecutableEvent) null;
        switch (event.getModificationType()) {
            case ADD:
                defaultEvent = level.getLocalizedEvent(player, LevelEventContainer::getExpReceivedEvent);
                if (defaultEvent != null) {
                    defaultEvent.replace("{final-exp}", String.valueOf(data.getMultiple() * event.getAmount()));
                }

                localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getExpReceivedEvent);
                if (localizedEvent != null) {
                    localizedEvent.replace("{final-exp}", String.valueOf(data.getMultiple() * event.getAmount()));
                }
                break;
            case REMOVE:
                defaultEvent = level.getDefaultEvent(LevelEventContainer::getLevelsRemovedEvent);
                localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getLevelsRemovedEvent);
                break;
            case SET:
                defaultEvent = level.getDefaultEvent(LevelEventContainer::getLevelsSetEvent);
                localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getLevelsSetEvent);
                break;
        }

        if (defaultEvent != null) {
            defaultEvent.replace("{exp}", String.valueOf(event.getAmount()));
            defaultEvent.execute(DreamLevels.getInstance(), player);
        }

        if (localizedEvent != null) {
            localizedEvent.replace("{exp}", String.valueOf(event.getAmount()));
            localizedEvent.execute(DreamLevels.getInstance(), player);
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
        var level = event.getLevel();
        var player = event.getPlayer();

        // default event
        var defaultEvent = level.getDefaultEvent(LevelEventContainer::getMultipleSetEvent);
        if (defaultEvent != null) {
            defaultEvent.replace("{multiple}", NumberUtils.cutString(event.getAmount(), 2));
            defaultEvent.execute(DreamLevels.getInstance(), player);
        }

        // localized event
        var localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getMultipleSetEvent);
        if (localizedEvent != null) {
            localizedEvent.replace("{multiple}", NumberUtils.cutString(event.getAmount(), 2));
            localizedEvent.execute(DreamLevels.getInstance(), player);
        }

        // save data
        DataManager.getInstance().getPlayerData(player).saveAsync();

        // update data spy menus
        DataSpyManager.getInstance().updateMenu(player, level);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDataReset(PlayerDataResetEvent event) {
        var level = event.getLevel();
        var player = event.getPlayer();

        var defaultEvent = level.getDefaultEvent(LevelEventContainer::getResetEventEvent);
        if (defaultEvent != null) {
            defaultEvent.execute(DreamLevels.getInstance(), player);
        }

        var localizedEvent = level.getLocalizedEvent(player, LevelEventContainer::getResetEventEvent);
        if (localizedEvent != null) {
            localizedEvent.execute(DreamLevels.getInstance(), player);
        }

        // save data
        DataManager.getInstance().getPlayerData(player).saveAsync();

        // update experience bar
        LevelManager.getInstance().updateExperienceBar(player);

        // update data spy menus
        DataSpyManager.getInstance().updateMenu(player, level);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDataResetAll(PlayerDataResetAllEvent event) {
        // execute localized event
        var localizedEvent = (ExecutableEvent) LocaleUtils.getLocale(event.getPlayer()).getProperty("level-reset-all");
        localizedEvent.execute(DreamLevels.getInstance(), event.getPlayer());

        // execute default event
        LevelManager.getInstance().getDefaultResetAllEvent().execute(DreamLevels.getInstance(), event.getPlayer());

        // save data
        DataManager.getInstance().getPlayerData(event.getPlayer()).saveAsync();

        // update experience bar
        LevelManager.getInstance().updateExperienceBar(event.getPlayer());

        // update data spy menus
        DataSpyManager.getInstance().updateMenus(event.getPlayer());
    }
}
