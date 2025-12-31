package net.dreampixel.dreamlevels.menu.dataspy;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataOverallMenu;
import net.dreampixel.dreamlevels.menu.dataspy.menu.PlayerDataMenu;
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

    // menu for showing all player data
    private PlayerDataMenu playerDataMenu;
    // menu for showing a player's all level data
    private final HashMap<UUID, LevelDataOverallMenu> levelDataOverallMenus = new HashMap<>();
    // the key is "{target-uuid}-{levelName}"
    private final HashMap<String, LevelDataMenu> levelDataMenus = new HashMap<>();

    public DataSpyManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        // load items
        var items = plugin.getItemsConfiguration().getNodeSection("data-spy-menu");
        if (items == null) {
            MLogger.error("data-spy.invalid-items");
            return;
        }

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
                });
            });

            menuItem.addClickAction(e -> e.setCancelled(true));
            this.items.put(key, menuItem);
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

    /**
     * Open the player data menu for a player.
     *
     * @param player Player
     */
    public void openPlayerDataMenu(@NotNull Player player) {
        getPlayerDataMenu().openMenu(player);
    }

    /**
     * Open the menu of the target player's all level data.
     *
     * @param player Player
     * @param target Target player's uniqueId
     */
    public void openLevelDataOverallMenu(@NotNull Player player, @NotNull UUID target) {
        var menu = getLevelDataOverallMenu(target);
        menu.openMenu(player);
    }

    /**
     * Open the menu of target player's level data of a specific level.
     *
     * @param player Player to show the menu
     * @param target Target player's uniqueId
     * @param level A level system
     */
    public void openLevelDataMenu(@NotNull Player player, @NotNull UUID target, @NotNull Level level) {
        var menu = getLevelDataMenu(target, level);
        menu.openMenu(player);
    }

    /**
     * Get the player data menu. A new one will be created if absent.
     *
     * @return The player data menu
     */
    @NotNull
    public PlayerDataMenu getPlayerDataMenu() {
        if (playerDataMenu == null) {
            playerDataMenu = new PlayerDataMenu("player-data-menu", "Player Data Menu");
            plugin.getDataSpyMenuHandler().addMenu(playerDataMenu);
        }

        return playerDataMenu;
    }

    /**
     * Open a specific player's level data menu.
     *
     * @param target The target player's uniqueId
     * @return LevelDataOverallMenu
     */
    public LevelDataOverallMenu getLevelDataOverallMenu(@NotNull UUID target) {
        if (this.levelDataOverallMenus.containsKey(target)) {
            return this.levelDataOverallMenus.get(target);
        }

        var menu = new LevelDataOverallMenu(target);
        this.levelDataOverallMenus.put(target, menu);
        return menu;
    }

    /**
     * @param target Target player
     * @param level Target level
     * @return A specific level data's menu
     */
    public LevelDataMenu getLevelDataMenu(@NotNull UUID target, @NotNull Level level) {
        var key = target + "-" + level.getName();
        if (this.levelDataMenus.containsKey(key)) {
            return this.levelDataMenus.get(key);
        }

        var menu = new LevelDataMenu("ldm-" + target, target, level);
        this.levelDataMenus.put(key, menu);
        return menu;
    }

    /**
     * Get a menu item with a specific item key defined in the "Items.yml" configuration.
     *
     * @param key Item key
     * @return Menu item
     */
    @Nullable
    public MenuItem getItemByKey(@NotNull String key) {
        return MapUtils.smartMatch(key, this.items);
    }

    /**
     * Update the menu of this player if existent, which will update all items in the menu.
     *
     * @param player Player
     * @param level Level
     */
    public void updateMenu(@NotNull Player player, @NotNull Level level) {
        var menu = levelDataMenus.get(player.getUniqueId() + "-" + level.getName());
        if (menu != null) {
            menu.updateItems();
        }
    }

    /**
     * Update all menus of the player, which will update all items in these menus.
     * Nothing will be done if there are no menus.
     *
     * @param player Player
     */
    public void updateMenus(@NotNull Player player) {
        levelDataMenus.forEach((k, v) -> {
            var uid = player.getUniqueId().toString();
            if (k.startsWith(uid)) {
                v.updateItems();
            }
        });
    }

    /**
     * Update all level-data-overall menus.
     */
    public void updateLDOMenus() {
        for (var uniqueId : levelDataOverallMenus.keySet()) {
            var ldom = new LevelDataOverallMenu(uniqueId);
            this.levelDataOverallMenus.put(uniqueId, ldom);
        }
    }

    @NotNull
    public static DataSpyManager getInstance() {
        return DreamLevels.getInstance().getDataSpyManager();
    }
}
