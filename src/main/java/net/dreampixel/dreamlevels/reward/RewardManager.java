package net.dreampixel.dreamlevels.reward;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;
import top.shadowpixel.shadowcore.api.config.manager.ConfigurationProperty;
import top.shadowpixel.shadowcore.api.config.manager.ConfigurationType;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.util.item.ItemBuilder;
import top.shadowpixel.shadowcore.object.interfaces.Manager;
import top.shadowpixel.shadowcore.util.ConfigurationUtils;
import top.shadowpixel.shadowcore.util.collection.MapUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class RewardManager implements Manager {
    private final DreamLevels plugin;

    private File directory;

    private final HashMap<String, RewardList> rewardLists = new HashMap<>();
    protected final HashMap<String, MenuItem> defaultItems = new HashMap<>();

    public RewardManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        // check configuration
        var config = plugin.getConfiguration().getNodeSection("reward");
        if (config == null) {
            return;
        }

        // check directory
        this.directory = new File(config.getString("directory")
                .replace("{default}", plugin.getDataFolder().getPath()));
        this.directory.mkdirs();
        if (!directory.isDirectory()) {
            MLogger.error("reward.invalid-directory");
            return;
        }

        // load and log
        loadAll();
        if (rewardLists.isEmpty()) {
            MLogger.infoReplaced("reward.load-empty");
        } else {
            MLogger.infoReplaced("reward.load-all",
                    "{amount}", String.valueOf(this.rewardLists.size()));
        }

        // load items
        loadDefaultItems();
    }

    @Override
    public void unload() {
        this.rewardLists.values().forEach(RewardList::clearMenus);
        this.rewardLists.clear();
        this.defaultItems.clear();
    }

    public void create(@NotNull String name, @NotNull String level) {
        var file = new File(this.directory, name + ".yml");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // load configuration from resource
                var property = ConfigurationProperty.builder()
                        .resourceSupplier(() -> plugin.getResource("default-reward.yml"))
                        .configurationType(ConfigurationType.YAML)
                        .storageFile(file)
                        .createNewFile()
                        .createParentDirs()
                        .build();
                // replace placeholders
                var config = property.loadFromResource();
                config.set("reward-list.name", name);
                config.set("reward-list.level", level);
                // save the reward list
                property.save(config);

                // enable the reward in configuration then save
                var configuration = this.plugin.getConfiguration();
                ConfigurationUtils.add(configuration, "reward.enabled", name);
                configuration.save();

                // create a reward list
                var rewardList = new RewardList(config, name);
                this.rewardLists.put(name, rewardList);

                MLogger.info("reward.create");
            } catch (Exception e) {
                MLogger.error("reward.create-failed", e);
            }
        });
    }

    /**
     * Load a reward from the reward directory.
     *
     * @param name Name
     * @param skipCheck Whether not to check this reward is enabled
     *                  in Config.yml when loading.
     */
    public void load(@NotNull String name, boolean skipCheck) {
        // check enabled
        if (!plugin.getConfiguration().getStringList("reward.enabled").contains(name) && !skipCheck) {
            return;
        }

        // load from the file
        var file = new File(this.directory, name + ".yml");
        load(file);
    }

    public void load(@NotNull File file) {
        try {
            var name = file.getName();
            name = name.substring(0, name.lastIndexOf(".")); // cut suffix

            var config = ConfigurationProvider.getYamlConfigurationProvider().load(file);
            // check inconsistent name
            var rewardName = config.getString("reward-list.name");
            if (!name.equals(rewardName)) {
                MLogger.errorReplaced("reward.inconsistent-name",
                        "{reward}", rewardName);
                return;
            }

            // check level
            var level = config.getString("reward-list.level");
            if (!LevelManager.getInstance().hasLevel(level)) {
                MLogger.warnReplaced("reward.load-skip",
                        "{reward}", name,
                        "{level}", level);
                return;
            }

            var rewardList = new RewardList(config, name);
            this.rewardLists.put(name, rewardList);
            MLogger.infoReplaced("reward.load",
                    "{reward}", name);
        } catch (IOException e) {
            MLogger.errorReplaced("reward.load-error",
                    "{file}", file.getPath());
            Logger.error(e);
        }
    }

    /**
     * Load all rewards from the reward directory.
     */
    public void loadAll() {
        if (this.directory == null || !this.directory.isDirectory()) {
            return;
        }

        var levels = this.directory.list();
        if (levels == null) {
            return;
        }

        for (var level : levels) {
            load(level.substring(0, level.lastIndexOf(".")), false);
        }
    }

    public void loadDefaultItems() {
        var items = requireNonNull(plugin.getConfiguration("Items"), "Items configuration is null");
        items.isNodeSection("items", section -> section.getKeys().forEach(key -> section.isNodeSection(key, itemSection -> {
            var item = ItemBuilder.builder(itemSection).build();
            this.defaultItems.put(key, MenuItem.of(item));
        })));
    }

    @Nullable
    public RewardList getRewardList(@NotNull String name) {
        return MapUtils.smartMatch(name, this.rewardLists);
    }

    @NotNull
    public HashMap<String, RewardList> getRewardLists() {
        return rewardLists;
    }

    @NotNull
    public List<RewardList> getRewardLists(@NotNull Level level) {
        return this.rewardLists.values().stream()
                .filter(t -> t.getLevel() == level)
                .collect(Collectors.toList());
    }

    public boolean hasRewardList(@NotNull String name) {
        return this.rewardLists.containsKey(name);
    }

    /**
     * Remove the player's all reward menus.
     */
    public void removeRewardMenu(@NotNull Player player) {
        this.rewardLists.values()
                .forEach(t -> t.removeRewardMenu(player));
    }

    public void updateRewardMenus(@NotNull Player player, Level level) {
        getRewardLists(level)
                .forEach(r -> {
                    var menu = r.findRewardMenu(player);
                    if (menu != null) {
                        menu.updateItems();
                    }
                });
    }

    @NotNull
    public List<RewardMenu> getRewardMenus(@NotNull Player player) {
        return this.rewardLists.values().stream()
                .map(t -> t.findRewardMenu(player))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NotNull
    public static RewardManager getInstance() {
        return DreamLevels.getInstance().getRewardManager();
    }
}
