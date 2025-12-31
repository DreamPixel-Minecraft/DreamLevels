package net.dreampixel.dreamlevels.menu.level;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.menu.level.menu.LevelModificationMenu;
import net.dreampixel.dreamlevels.menu.level.menu.LevelOverallMenu;
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

public class LevelSpyManager implements Manager {
    private final DreamLevels plugin;
    private final HashMap<String, MenuItem> items = new HashMap<>();

    // menu showing all player data
    private LevelOverallMenu levelOverallMenu;
    // the key is level-name
    private final HashMap<String, LevelModificationMenu> levelModificationMenus = new HashMap<>();

    public LevelSpyManager(DreamLevels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        // load items
        var items = plugin.getItemsConfiguration().getNodeSection("level-spy-menu");
        if (items == null) {
            MLogger.error("level-spy.invalid-items");
            return;
        }

        // iter all node sections under "levelspy-menu"
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

            menuItem.addClickAction(e -> e.setCancelled(true));
            this.items.put(key, menuItem);
        }
    }

    @Override
    public void unload() {
        items.clear();

        // clear menus
        levelOverallMenu = null;
        levelModificationMenus.clear();

        // delete menus in menu handlers
        plugin.getLevelSpyMenuHandler().deleteMenus();
    }

    /**
     * Open the levels menu for a player.
     *
     * @param player Player
     */
    public void openLevelOverallMenu(@NotNull Player player) {
        getLevelOverallMenu().openMenu(player);
    }

    /**
     * Open a menu for modifying the specific level system.
     *
     * @param player Player to show the menu
     * @param level Level system to modify
     */
    public void openLevelMenu(@NotNull Player player, @NotNull Level level) {
        getLevelModificationMenu(level).openMenu(player);
    }

    /**
     * Get the menu showing all levels, or create one if absent.
     *
     * @return The menu showing all levels
     */
    @NotNull
    public LevelOverallMenu getLevelOverallMenu() {
        if (levelOverallMenu == null) {
            updateLevelOverallMenu();
        }

        return levelOverallMenu;
    }

    /**
     * Get the menu for modifying the level system. A new one will be created if absent.
     *
     * @param level Level system to modify
     * @return The menu for modifying the level system
     */
    @NotNull
    public LevelModificationMenu getLevelModificationMenu(@NotNull Level level) {
        var menu = levelModificationMenus.get(level.getName());
        if (menu == null) {
            menu = new LevelModificationMenu(level);
            levelModificationMenus.put(level.getName(), menu);
        }

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
     * Update the menu for a specific level system if present.
     *
     * @param level Level system
     */
    public void updateMenu(@NotNull Level level) {
        var menu = levelModificationMenus.get(level.getName());
        if (menu != null) {
            menu.updateItems();
        }
    }

    /**
     * Update the menu showing all levels.
     */
    public void updateLevelOverallMenu() {
        this.levelOverallMenu = new LevelOverallMenu();
        plugin.getLevelSpyMenuHandler().addMenu(levelOverallMenu);
    }

    @NotNull
    public static LevelSpyManager getInstance() {
        return DreamLevels.getInstance().getLevelSpyManager();
    }
}
