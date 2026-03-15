package net.dreampixel.dreamlevels.menu.dataspy.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.dataspy.item.DSItemExp;
import net.dreampixel.dreamlevels.menu.dataspy.item.DSItemLevels;
import net.dreampixel.dreamlevels.menu.dataspy.item.DSItemMultiple;
import net.dreampixel.dreamlevels.menu.dataspy.item.DSItemReset;
import net.dreampixel.dreamlevels.task.lifecycle.LifeCycleTask;
import net.dreampixel.dreamlevels.task.lifecycle.LifeCycled;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;
import java.util.UUID;

import static net.dreampixel.dreamlevels.menu.dataspy.menu.PlayerDataMenu.getItemByKey;

/**
 * A menu for a player's single level data.
 */
public class LevelDataMenu extends GlobalMenu implements LifeCycled {
    private final UUID uniqueId;
    private final Level level;

    private DSItemLevels levelsItem;
    private DSItemExp expItem;
    private DSItemMultiple multipleItem;

    // life cycle
    private int lifeCycle;

//    private ReceivedRewardsItem receivedRewardsItem;

    public LevelDataMenu(String name, UUID uniqueId, Level level) {
        super(DreamLevels.getInstance().getDataSpyMenuHandler(),
                name,
                "Level Data Menu");
        this.uniqueId = uniqueId;
        this.level = level;

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
        levelsItem = new DSItemLevels(this);
        expItem = new DSItemExp(this);
        multipleItem = new DSItemMultiple(this);

        page.setItem(10, levelsItem);
        page.setItem(11, expItem);
        page.setItem(12, multipleItem);
        page.setItem(28, new DSItemReset(this));
    }

    public void updateItems() {
        levelsItem.updateItem();
        expItem.updateItem();
        multipleItem.updateItem();
    }

    @Override
    public int getLifeCycle() {
        return lifeCycle;
    }

    @Override
    public void setLifeCycle(int lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public void remove() {
        DataSpyManager.getInstance().removeLevelDataMenu(getKey());
    }

    /**
     * @return The key of the menu stored in data spy manager's map
     */
    @NotNull
    public String getKey() {
        return getName();
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
