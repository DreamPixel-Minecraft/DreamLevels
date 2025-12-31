package net.dreampixel.dreamlevels.menu.level.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.menu.level.item.LSItemDefaultLevels;
import net.dreampixel.dreamlevels.menu.level.item.LSItemDisplayName;
import net.dreampixel.dreamlevels.menu.level.item.LSItemMaxLevels;
import net.dreampixel.dreamlevels.menu.level.item.LSItemRequiredExp;
import net.dreampixel.dreamlevels.util.Logger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import static net.dreampixel.dreamlevels.menu.level.menu.LevelOverallMenu.getItemByKey;

public class LevelModificationMenu extends GlobalMenu {
    private final Level level;

    //items
    private LSItemDefaultLevels defaultLevelsItem;
    private LSItemDisplayName displayNameItem;
    private LSItemMaxLevels maxLevelsItem;
    private LSItemRequiredExp requiredExpItem;

    public LevelModificationMenu(@NotNull Level level) {
        super("levelMenu-" + level.getName(), "idk");
        this.level = level;
        constructMenu();
    }

    public void constructMenu() {
        var plugin = DreamLevels.getInstance();

        // create page
        var title = ReplaceUtils.coloredReplace(
                DreamLevels.getInstance().getConfiguration().getString("level-spy.menu-title.level"),
                "{level}", level.getName());
        var page = addPage(title, 54);

        // put custom items
        var itemSlots = plugin.getConfiguration().getNodeSection("level-spy.item-slots.level");
        if (itemSlots == null) {
            return;
        }

        for (var itemKey : itemSlots.getKeys()) {
            var slots = itemSlots.getIntegerList(itemKey);
            var customItem = getItemByKey(itemKey);
            if (customItem == null) {
                Logger.error("Unknown custom item for level spy menu: " + itemKey);
                continue;
            }

            // put items into every page
            for (var slot : slots) {
                page.setItem(slot, customItem);
            }
        }

        // put items
        defaultLevelsItem = new LSItemDefaultLevels(this);
        displayNameItem = new LSItemDisplayName(this);
        maxLevelsItem = new LSItemMaxLevels(this);
        requiredExpItem = new LSItemRequiredExp(this);

        page.setItem(10, defaultLevelsItem);
        page.setItem(11, displayNameItem);
        page.setItem(12, maxLevelsItem);
        page.setItem(13, requiredExpItem);
    }

    public void updateItems() {
        defaultLevelsItem.updateItem();
        displayNameItem.updateItem();
        maxLevelsItem.updateItem();
        requiredExpItem.updateItem();
    }

    @NotNull
    public Level getLevel() {
        return level;
    }
}
