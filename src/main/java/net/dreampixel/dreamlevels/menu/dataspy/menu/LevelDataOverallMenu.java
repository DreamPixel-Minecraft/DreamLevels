package net.dreampixel.dreamlevels.menu.dataspy.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.dataspy.item.DSItemResetAll;
import net.dreampixel.dreamlevels.task.lifecycle.LifeCycleTask;
import net.dreampixel.dreamlevels.task.lifecycle.LifeCycled;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.NodeSection;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static net.dreampixel.dreamlevels.menu.dataspy.menu.PlayerDataMenu.dataSlots;
import static net.dreampixel.dreamlevels.menu.dataspy.menu.PlayerDataMenu.getItemByKey;

/**
 * A menu for a player's all level data.
 */
public class LevelDataOverallMenu extends GlobalMenu implements LifeCycled {
    private final UUID uniqueId;
    private int lifeCycle;

    public LevelDataOverallMenu(UUID uniqueId) {
        super(DreamLevels.getInstance().getDataSpyMenuHandler(),
                "ldom-" + uniqueId,
                "Level Data Menu");
        this.uniqueId = uniqueId;
        constructMenu();
    }

    @Override
    protected void onOpened() {
        LifeCycleTask.remove(getKey());
    }

    @Override
    protected void onClosed() {
        LifeCycleTask.add(this);
    }

    public void constructMenu() {
        var plugin = DreamLevels.getInstance();

        // check the player data
        var player = Bukkit.getPlayer(uniqueId);
        var playerData = DataManager.getInstance().getPlayerData(uniqueId);
        if (player == null || playerData == null) {
            return;
        }

        var targets = playerData.getLevelData().values();

        // check custom items' slots
        var itemSlots = plugin.getConfiguration().getNodeSection("data-spy.item-slots.level-data-overall");
        if (itemSlots == null) {
            return;
        }

        setPages(targets, itemSlots);

        // put level data items
        setLevelDataItems(targets, player);

        // put custom items
        setCustomItems(itemSlots);
    }

    /**
     * Remove the menu. This method will remove the menu from data spy manager and delete it.
     */
    public void remove() {
        DataSpyManager.getInstance().removeLevelDataOverallMenu(uniqueId);
    }

    @Override
    public String getKey() {
        return getName();
    }

    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public int getLifeCycle() {
        return lifeCycle;
    }

    @Override
    public void setLifeCycle(int lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    private void setPages(Collection<LevelData> targets, NodeSection itemSlots) {
        var plugin = DreamLevels.getInstance();
        var raItem = new DSItemResetAll(this);

        // create pages
        var pageCount = targets.size() / dataSlots.length
                + (targets.size() % dataSlots.length > 0 ? 1 : 0);
        for (int i = 1; i <= pageCount; i++) {
            var title = ReplaceUtils.coloredReplace(plugin.getConfiguration().getString("data-spy.menu-title.level-data-overall"),
                    "{page}", String.valueOf(i));
            var page = addPage(i, title, 54);
            // copy immutable variable
            var finalI = i;

            // next-page item
            if (i != getPages().size()) {
                var item = getItemByKey("next-page");
                if (item != null) {
                    item.addClickAction(e -> {
                        changePage(e.getPlayer(), finalI + 1);
                        e.setCancelled(true);
                    });

                    itemSlots.isIntegerList("next-page",
                            list -> list.forEach(slot -> setItem(finalI, slot, item)));
                }
            }

            // previous-page item
            if (i > 1) {
                var item = getItemByKey("previous-page");
                if (item != null) {
                    item.addClickAction(e -> {
                        changePage(e.getPlayer(), finalI - 1);
                        e.setCancelled(true);
                    });

                    itemSlots.isIntegerList("previous-page",
                            list -> list.forEach(slot -> setItem(finalI, slot, item)));
                }
            }

            if (page != null) {
                page.setItem(49, raItem);
            }
        }
    }

    private void setLevelDataItems(Collection<LevelData> targets, Player player) {
        var dataItem = getItemByKey("level-data");
        if (dataItem == null) {
            return;
        }

        var count = 0;
        var idx = 0;
        var currentPage = 1;
        for (var target : targets) {
            // create copy from dataItem, replace "{player}"
            var item = ItemUtils.replace(dataItem.clone(),
                    "{player}", player.getName(),
                    "{level}", target.getLevelName());
            item.addClickAction(e -> {
                DataSpyManager.getInstance().openLevelDataMenu(e.getPlayer(), target.getUniqueId(), Objects.requireNonNull(target.getLevel()));
                e.setCancelled(true);
            });

            setItem(currentPage, dataSlots[idx], item);

            // next page
            idx++;
            if (count++ >= dataSlots.length) {
                currentPage++;
                idx = 0;
            }
        }
    }

    private void setCustomItems(NodeSection itemSlots) {
        for (var itemKey : itemSlots.getKeys()) {
            if (itemKey.equals("next-page") || itemKey.equals("previous-page")) {
                continue;
            }

            var slots = itemSlots.getIntegerList(itemKey);
            var customItem = getItemByKey(itemKey);
            if (customItem == null) {
                Logger.error("Unknown custom item for data spy menu: " + itemKey);
                continue;
            }

            // put items into every page
            for (var page : getPages().values()) {
                for (var slot : slots) {
                    page.setItem(slot, customItem);
                }
            }
        }
    }
}
