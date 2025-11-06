package net.dreampixel.dreamlevels.dataspy.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.dataspy.item.ExpItem;
import net.dreampixel.dreamlevels.dataspy.item.LevelsItem;
import net.dreampixel.dreamlevels.dataspy.item.MultipleItem;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;
import java.util.UUID;

import static net.dreampixel.dreamlevels.dataspy.menu.PlayerDataMenu.getItemByKey;

/**
 * A menu for a player's single level data.
 */
public class LevelDataMenu extends GlobalMenu {
    private final UUID uniqueId;
    private final Level level;

    private LevelsItem levelsItem;
    private ExpItem expItem;
    private MultipleItem multipleItem;

//    private ReceivedRewardsItem receivedRewardsItem;

    public LevelDataMenu(String name, UUID uniqueId, Level level) {
        super(name, "Level Data Menu");
        this.uniqueId = uniqueId;
        this.level = level;

        constructMenu();
    }

    public void constructMenu() {
        var player = Bukkit.getPlayer(uniqueId);
        if (player == null) {
            return;
        }

        var name = Objects.requireNonNull(Bukkit.getPlayer(uniqueId)).getName();
        var title = ReplaceUtils.coloredReplace(
                DreamLevels.getInstance().getConfiguration().getString("data-spy.menu-title.level-data"),
                player, "{player}", name);
        var page = addPage(title, 54);

        // put custom items
        var plugin = DreamLevels.getInstance();
        var itemSlots = plugin.getConfiguration().getNodeSection("data-spy.item-slots.level-data-overall");
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
            for (var slot : slots) {
                page.setItem(slot, customItem);
            }
        }

        // set items
        levelsItem = new LevelsItem(this);
        expItem = new ExpItem(this);
        multipleItem = new MultipleItem(this);

        page.setItem(10, levelsItem);
        page.setItem(11, expItem);
        page.setItem(12, multipleItem);
    }

    public void updateItems() {
        levelsItem.updateItem();
        expItem.updateItem();
        multipleItem.updateItem();
    }

    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    @NotNull
    public Level getLevel() {
        return level;
    }
}
