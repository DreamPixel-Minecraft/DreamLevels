package net.dreampixel.dreamlevels.dataspy;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.dataspy.menu.LevelDataOverallMenu;
import net.dreampixel.dreamlevels.dataspy.menu.PlayerDataMenu;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.util.item.ItemBuilder;
import top.shadowpixel.shadowcore.object.interfaces.Manager;
import top.shadowpixel.shadowcore.util.collection.MapUtils;

import java.util.HashMap;
import java.util.UUID;

public class DataSpyManager implements Manager {
    private final DreamLevels plugin;
    private final HashMap<String, MenuItem> items = new HashMap<>();

    // menu showing all player data
    private PlayerDataMenu playerDataMenu;
    // menu showing a player's all level data
    private final HashMap<UUID, LevelDataOverallMenu> levelDataOverallMenus = new HashMap<>();
    // the key is "{target-uuid}-{levelName}"
    private final HashMap<String, LevelDataMenu> levelDataMenus = new HashMap<>();

    public DataSpyManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        // load items
        var items = plugin.getItemsConfiguration().getNodeSection("dataspy-menu");
        if (items == null) {
            MLogger.error("dataspy.invalid-items");
        } else {
            // iter all node sections under "dataspy-menu"
            // that's to say, there could be custom items
            for (var key : items.getKeys()) {
                var item = items.getNodeSection(key);
                if (item == null) {
                    continue;
                }

                // build item stack and then turn into menu item
                var itemStack = ItemBuilder.builder(item).build();
                var menuItem = new MenuItem(itemStack);

                // add custom click events
                item.isStringList("events", events -> {
                    var event = ExecutableEvent.of(events);
                    menuItem.addClickAction(e -> {
                        event.execute(plugin, e.getPlayer());
                        e.setCancelled(true);
                    });
                });

                this.items.put(key, menuItem);
            }
        }
    }

    @Override
    public void unload() {
        items.clear();

        // clear menus
        playerDataMenu = null;

        levelDataMenus.clear();
        levelDataOverallMenus.clear();

        // delete menus in menu handlers
        plugin.getDataSpyMenuHandler().deleteMenus();
    }

    public void openPlayerDataMenu(@NotNull Player player) {
        if (playerDataMenu == null) {
            playerDataMenu = new PlayerDataMenu("player-data-menu", "Player Data Menu");
        }

        playerDataMenu.openMenu(player);
        plugin.getDataSpyMenuHandler().addMenu(playerDataMenu);
    }

    public void openLevelDataOverallMenu(@NotNull Player player, @NotNull UUID target) {
        var menu = getLevelDataOverallMenu(target);
        menu.openMenu(player);
    }

    public void openLevelDataMenu(@NotNull Player player, @NotNull UUID target, @NotNull Level level) {
        var menu = getLevelDataMenu(target, level);
        menu.openMenu(player);
    }

    public LevelDataOverallMenu getLevelDataOverallMenu(@NotNull UUID target) {
        if (this.levelDataOverallMenus.containsKey(target)) {
            return this.levelDataOverallMenus.get(target);
        }

        var menu = new LevelDataOverallMenu("ldm-" + target, target);
        this.levelDataOverallMenus.put(target, menu);
        return menu;
    }

    public LevelDataMenu getLevelDataMenu(@NotNull UUID target, @NotNull Level level) {
        var key = target + "-" + level.getName();
        if (this.levelDataMenus.containsKey(key)) {
            return this.levelDataMenus.get(key);
        }

        var menu = new LevelDataMenu("ldm-" + target, target, level);
        this.levelDataMenus.put(key, menu);
        return menu;
    }

    @Nullable
    public MenuItem getItemByKey(@NotNull String key) {
        return MapUtils.smartMatch(key, this.items);
    }

    @NotNull
    public static DataSpyManager getInstance() {
        return DreamLevels.getInstance().getDataSpyManager();
    }

    public void updateMenu(@NotNull Player player, @NotNull Level level) {
        var menu = levelDataMenus.get(player.getUniqueId() + "-" + level.getName());
        if (menu != null) {
            menu.updateItems();
        }
    }

    public void updateMenus(@NotNull Player player) {
        levelDataMenus.forEach((k, v) -> {
            var uid = player.getUniqueId().toString();
            if (k.startsWith(uid)) {
                v.updateItems();
            }
        });
    }
}
