package net.dreampixel.dreamlevels.menu.level.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
import net.dreampixel.dreamlevels.util.Logger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.NodeSection;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Collection;

import static net.dreampixel.dreamlevels.menu.dataspy.menu.PlayerDataMenu.dataSlots;

/**
 * A menu for showing all available level systems.
 */
public class LevelOverallMenu extends GlobalMenu {
    public LevelOverallMenu() {
        super(DreamLevels.getInstance().getLevelSpyMenuHandler(),
                "overall levels menu",
                "Overall Level Menu");
        constructMenu();
    }

    public void constructMenu() {
        var plugin = DreamLevels.getInstance();
        var targets = LevelManager.getInstance().getLevels().values();

        // put custom items
        var itemSlots = plugin.getConfiguration().getNodeSection("level-spy.item-slots.level-overall");
        if (itemSlots == null) {
            return;
        }

        // create pages
        setPages(targets, itemSlots);

        // put level items
        setLevelItems(targets);

        // put custom items
        setCustomItems(itemSlots);
    }

    private void setLevelItems(Collection<Level> targets) {
        var dataItem = getItemByKey("level");

        // put level data items
        var count = 0;
        var idx = 0;
        var currentPage = 1;
        for (var target : targets) {
            // create copy from dataItem, replace "{player}"
            var item = ItemUtils.replace(dataItem.clone(),
                    "{level}", target.getName());
            // add click action that open the specific menu
            item.addClickAction(e -> {
                e.setCancelled(true);
                LevelSpyManager.getInstance().openLevelMenu(e.getPlayer(), target);
            });

            // put the item
            setItem(currentPage, dataSlots[idx], item);

            // change to the next page
            idx++;
            if (count++ >= dataSlots.length) {
                currentPage++;
                idx = 0;
            }
        }
    }

    private void setPages(Collection<Level> targets, NodeSection itemSlots) {
        var plugin = DreamLevels.getInstance();
        var pageCount = targets.size() / dataSlots.length
                + (targets.size() % dataSlots.length > 0 ? 1 : 0);
        for (int i = 1; i <= pageCount; i++) {
            var title = ReplaceUtils.coloredReplace(
                    plugin.getConfiguration().getString("level-spy.menu-title.level-overall"),
                    "{page}", String.valueOf(i));
            var page = addPage(i, title, 54);

            // copy immutable variable
            var finalI = i;

            // next-page item
            if (i != getPages().size()) {
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

    private void setCustomItems(NodeSection itemSlots) {
        for (var itemKey : itemSlots.getKeys()) {
            if (itemKey.equals("next-page") || itemKey.equals("previous-page")) {
                continue;
            }

            var slots = itemSlots.getIntegerList(itemKey);
            var customItem = getItemByKey(itemKey);
            if (customItem == null) {
                Logger.error("Unknown custom item for level spy menu: " + itemKey);
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
        return LevelSpyManager.getInstance().getItemByKey(itemKey);
    }
}
