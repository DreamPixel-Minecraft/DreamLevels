package net.dreampixel.dreamlevels.reward;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.NodeSection;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.api.menu.impl.PlayerMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemBuilder;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RewardList {
    private final NodeSection configuration;

    private final String name;
    private final Level level ;
    private final int size;
    private final HashMap<String, String> titles = new HashMap<>();
    private final ArrayList<String> permissions = new ArrayList<>();

    private final ArrayList<Reward> rewards = new ArrayList<>();
    private final HashMap<String, MenuItem> customItems = new HashMap<>();

    // reward menus
    private final HashMap<Player, RewardMenu> rewardMenus = new HashMap<>();

    public RewardList(@NotNull NodeSection configuration, @NotNull String name) {
        this.configuration = configuration;
        this.name = configuration.getString("reward-list.name");
        this.level = LevelManager.getInstance().getLevel(configuration.getString("reward-list.level"));
        this.size = configuration.getInt("reward-list.size", 54);
        this.permissions.addAll(configuration.getStringList("reward-list.permissions"));

        // put all titles
        titles.put("_DEFAULT_", this.name);
        configuration.isNodeSection("reward-list.titles", section -> section.getValues().forEach((k, v) -> titles.put(k, v.toString())));

        loadCustomItems();
        loadRewards();
    }

    public void loadCustomItems() {
        configuration.isNodeSection("reward-list.custom-items", s -> {
            for (var key : s.getKeys()) {
                var custom = s.getNodeSection(key);
                if (custom == null) {
                    return;
                }

                var item = ItemBuilder.builder(custom).build();
                var menuItem = MenuItem.of(item);

                // add click events
                var events = custom.getStringList("events");
                if (!events.isEmpty()) {
                    var ee = ExecutableEvent.of(events);
                    menuItem.addClickAction((menu, event) -> {
                        ee.execute(DreamLevels.getInstance(), (Player) event.getWhoClicked());
                        event.setCancelled(true);
                    });
                }

                this.customItems.put(key, menuItem);
            }
        });
    }

    public void loadRewards() {
        configuration.isNodeSection("reward-list.rewards", rewards -> rewards.getKeys().forEach(key -> {
            var reward_section = rewards.getNodeSection(key);
            if (reward_section != null) {
                var reward = new Reward(this, reward_section);
                this.rewards.add(reward);
            }
        }));
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Level getLevel() {
        return level;
    }

    @NotNull
    public ArrayList<Reward> getRewards() {
        return rewards;
    }

    /**
     * Get a title of a specific page. Returns the default title if this page
     * does not have a specific title.
     */
    @NotNull
    public String getTitle(int page) {
        var title = this.titles.get(String.valueOf(page));
        if (title == null) {
            title = this.titles.get("_DEFAULT_");
        }

        if (title == null) {
            title = this.name;
        }

        return title;
    }

    @NotNull
    public HashMap<String, String> getTitles() {
        return titles;
    }

    @NotNull
    public HashMap<String, MenuItem> getCustomItems() {
        return customItems;
    }

    public int getMenuSize() {
        return size;
    }

    @NotNull
    public ArrayList<String> getPermissions() {
        return permissions;
    }


    @NotNull
    public MenuItem getItemByKey(@NotNull String key) {
        // get from the reward list
        var item = this.customItems.get(key);
        // get from the default item configuration
        if (item == null) {
            item = RewardManager.getInstance().defaultItems.get(key);
        }
        // clone
        item = Objects.requireNonNull(item, "unknown reward item " + key).clone();

        return item;
    }

    @Nullable
    public RewardMenu findRewardMenu(@NotNull Player player) {
        return rewardMenus.get(player);
    }

    @NotNull
    public RewardMenu getRewardMenu(@NotNull Player player) {
        var menu = rewardMenus.get(player);
        if (menu == null) {
            menu = new RewardMenu(player, this);
            rewardMenus.put(player, menu);
        }

        return menu;
    }

    public void openRewardMenu(@NotNull Player player) {
        getRewardMenu(player).openMenu();
    }

    public void removeRewardMenu(@NotNull Player player) {
        var menu = this.rewardMenus.remove(player);
        if (menu != null) {
            menu.delete();
        }
    }

    /**
     * Close and clear all player menus.
     */
    public void clearMenus() {
        this.rewardMenus.values().forEach(PlayerMenu::delete);
        this.rewardMenus.clear();
    }

    @NotNull
    public NodeSection getConfiguration() {
        return configuration;
    }
}
