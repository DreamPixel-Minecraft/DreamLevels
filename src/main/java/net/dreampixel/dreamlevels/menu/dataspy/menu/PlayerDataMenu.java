package net.dreampixel.dreamlevels.menu.dataspy.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.NodeSection;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Collection;

/**
 * A
 */
public class PlayerDataMenu extends GlobalMenu {
    public static final int[] dataSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public PlayerDataMenu(String name, String title) {
        super(DreamLevels.getInstance().getDataSpyMenuHandler(),
                name,
                title);
        constructMenu();
    }

    public void constructMenu() {
        var plugin = DreamLevels.getInstance();
        var targets = Bukkit.getOnlinePlayers();

        var itemSlots = plugin.getConfiguration()
                .getNodeSection("data-spy.item-slots.player-data");
        if (itemSlots == null) {
            return;
        }

        // create pages
        setPages(targets, itemSlots);

        // put data items
        setDataItems(targets);

        // put custom items
        setCustomItems(itemSlots);
    }

    /**
     * Create pages.
     */
    public void setPages(Collection<? extends Player> targets, NodeSection itemSlots) {
        var pageCount = targets.size() / dataSlots.length
                + (targets.size() % dataSlots.length > 0 ? 1 : 0);
        for (int i = 1; i <= pageCount; i++) {
            addPage(i,
                    ReplaceUtils.coloredReplace(DreamLevels.getInstance().getConfiguration().getString("data-spy.menu-title.player-data"),
                            "{page}", String.valueOf(i)),
                    54);

            // copy immutable variable
            var finalI = i;

            // next-page item
            if (i != pageCount) {
                var item = getItemByKey("next-page");
                if (item != null) {
                    item.addClickAction(e -> changePage(e.getPlayer(), finalI + 1));
                    itemSlots.isIntegerList("next-page",
                            list -> list.forEach(slot -> setItem(finalI, slot, item)));
                }
            }

            // previous-page item
            if (i > 1) {
                var item = getItemByKey("previous-page");
                if (item != null) {
                    item.addClickAction(e -> changePage(e.getPlayer(), finalI - 1));
                    itemSlots.isIntegerList("previous-page",
                            list -> list.forEach(slot -> setItem(finalI, slot, item)));
                }
            }
        }
    }

    /**
     * Put data items into this menu.
     */
    public void setDataItems(Collection<? extends Player> targets) {
        var dataItem = getItemByKey("player-data");
        if (dataItem == null) {
            return;
        }

        var count = 0;
        var idx = 0;
        var currentPage = 1;
        for (var target : targets) {
            // create copy from dataItem, replace "{player}"
            var item = ItemUtils.replace(dataItem.clone(),
                    "{player}", target.getName());
            item.addClickAction(e -> {
                DataSpyManager.getInstance().openLevelDataOverallMenu(e.getPlayer(), target.getUniqueId());
                e.setCancelled(true);
            });

            setItem(currentPage, dataSlots[idx], item);
            idx++;
            if (count++ >= dataSlots.length) {
                currentPage++;
                idx = 0;
            }
        }
    }

    /**
     * Put custom items into this menu.
     *
     * @param itemSlots ItemSlots configuration
     */
    public void setCustomItems(NodeSection itemSlots) {
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

    public static MenuItem getItemByKey(@NotNull String itemKey) {
        return DataSpyManager.getInstance().getItemByKey(itemKey);
    }
}
