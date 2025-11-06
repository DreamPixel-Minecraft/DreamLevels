package net.dreampixel.dreamlevels.dataspy.menu;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.menu.impl.GlobalMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;
import java.util.UUID;

import static net.dreampixel.dreamlevels.dataspy.menu.PlayerDataMenu.dataSlots;
import static net.dreampixel.dreamlevels.dataspy.menu.PlayerDataMenu.getItemByKey;

/**
 * A menu for a player's all level data.
 */
public class LevelDataOverallMenu extends GlobalMenu {
    private final UUID uniqueId;

    public LevelDataOverallMenu(String name, UUID uniqueId) {
        super(name, "Level Data Menu");
        this.uniqueId = uniqueId;
        constructMenu();
    }

    @Override
    public void handleEvent(@NotNull InventoryEvent event) {
        super.handleEvent(event);
    }

    public void constructMenu() {
        var plugin = DreamLevels.getInstance();
        var dsManager = DataSpyManager.getInstance();

        var player = Bukkit.getPlayer(uniqueId);
        var playerData = DataManager.getInstance().getPlayerData(uniqueId);
        if (player == null || playerData == null) {
            return;
        }

        var targets = playerData.getLevelData().values();

        // create pages
        var pageCount = targets.size() / dataSlots.length
                + (targets.size() % dataSlots.length > 0 ? 1 : 0);
        for (int i = 1; i <= pageCount; i++) {
            addPage(i,
                    ReplaceUtils.coloredReplace(plugin.getConfiguration().getString("data-spy.menu-title.level-data-overall"),
                            "{page}", String.valueOf(i)),
                    54);
        }

        var dataItem = getItemByKey("level-data");

        // put data items
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

        // put custom items
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
            for (var page : getPages().values()) {
                for (var slot : slots) {
                    page.setItem(slot, customItem);
                }
            }
        }
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
}
