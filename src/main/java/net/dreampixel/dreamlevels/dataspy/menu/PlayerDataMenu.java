package net.dreampixel.dreamlevels.dataspy.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

/**
 * A
 */
public class PlayerDataMenu extends GlobalMenu {
    protected static final int[] dataSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public PlayerDataMenu(String name, String title) {
        super(name, title);
        constructMenu();
    }

    public void constructMenu() {
        var plugin = DreamLevels.getInstance();
        var dsManager = DataSpyManager.getInstance();

        var targets = Bukkit.getOnlinePlayers();

        // create pages
        var pageCount = targets.size() / dataSlots.length
                + (targets.size() % dataSlots.length > 0 ? 1 : 0);
        for (int i = 1; i <= pageCount; i++) {
            addPage(i,
                    ReplaceUtils.coloredReplace(plugin.getConfiguration().getString("data-spy.menu-title.player-data"),
                            "{page}", String.valueOf(i)),
                    54);
        }

        var dataItem = getItemByKey("player-data");

        // put data items
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
            // next page
            idx++;
            if (count++ >= dataSlots.length) {
                currentPage++;
                idx = 0;
            }
        }

        // put custom items
        var itemSlots = plugin.getConfiguration().getNodeSection("data-spy.item-slots.player-data");
        if (itemSlots == null) {
            return;
        }

        for (var itemKey : itemSlots.getKeys()) {
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

    protected static MenuItem getItemByKey(@NotNull String itemKey) {
        return DataSpyManager.getInstance().getItemByKey(itemKey);
    }
}
