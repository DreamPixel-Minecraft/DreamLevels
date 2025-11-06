package net.dreampixel.dreamlevels.level;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.level.task.ExperienceBarTask;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.object.interfaces.Manager;
import top.shadowpixel.shadowcore.util.ConfigurationUtils;
import top.shadowpixel.shadowcore.util.collection.MapUtils;
import top.shadowpixel.shadowcore.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class LevelManager implements Manager {
    private final DreamLevels plugin;

    private final HashMap<String, Level> levels = new HashMap<>();
    private File directory;

    /**
     * Experience bar fields
     */
    private ExperienceBarTask experienceBarTask;
    private Level experienceBarLevel;
    private List<String> activatedWorlds;

    public LevelManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        // check configuration
        var config = plugin.getConfiguration().getNodeSection("level");
        if (config == null) {
            return;
        }

        // make and check directory
        this.directory = new File(config.getString("directory")
                .replace("{default}", plugin.getDataFolder().getPath()));
        this.directory.mkdirs();
        if (!directory.isDirectory()) {
            MLogger.error("level.invalid-directory");
            return;
        }

        // load and log
        loadAll();
        if (levels.isEmpty()) {
            MLogger.infoReplaced("level.load-empty");
        } else {
            MLogger.infoReplaced("level.load-all",
                    "{amount}", String.valueOf(this.levels.size()));
        }

        // experience bar task
        if (config.getBoolean("experience-bar.enabled")) {
            this.experienceBarLevel = getLevel(config.getString("experience-bar.level"));
            if (experienceBarLevel == null) {
                Logger.error("The specified level of experience bar task is invalid, and the exp bar service has been down!");
                return;
            }

            startExperienceBarTask();
            this.activatedWorlds = config.getStringList("experience-bar.worlds");
        }
    }

    @Override
    public void unload() {
        stopExperienceBarTask();
        this.levels.clear();
    }

    public void create(@NotNull String name, @NotNull String displayName) {
        var file = new File(this.directory, name + ".yml");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // read lines and replace $name$ and $display-name$
                var lines = FileUtils.readAllLines(requireNonNull(plugin.getResource("default-level.yml")),
                        "$name$", name,
                        "$display-name$", displayName);
                // create file and write
                file.createNewFile();
                Files.write(file.toPath(), lines);

                // enable the level in configuration then save
                var configuration = this.plugin.getConfiguration();
                ConfigurationUtils.add(configuration, "level.enabled", name);
                configuration.save();

                // create an empty level and load
                var level = new Level(name);
                this.levels.put(name, level);

                // add empty level data to online players
                DataManager.getInstance().getLoadedData().values().forEach(v ->
                        v.getLevelData().put(name, new LevelData(v.getUniqueId(), name)));

                MLogger.info("level.create");
            } catch (Exception e) {
                MLogger.error("level.create-failed", e);
            }
        });
    }

    /**
     * Load a level from the level directory.
     *
     * @param name Name
     * @param skipCheck Whether not to check this level is enabled
     *                  in Config.yml when loading.
     */
    public void load(@NotNull String name, boolean skipCheck) {
        // check enabled
        if (!plugin.getConfiguration().getStringList("level.enabled").contains(name) && !skipCheck) {
            return;
        }

        // load from the file
        var file = new File(this.directory, name + ".yml");
        load(file);
    }

    /**
     * Load a level from a specific file, ignoring enablement check.
     */
    public void load(@NotNull File file) {
        try {
            var name = file.getName();
            name = name.substring(0, name.lastIndexOf(".")); // cut suffix

            var config = ConfigurationProvider.getYamlConfigurationProvider().load(file);
            var level = (Level) config.get("level");
            if (level == null) {
                MLogger.errorReplaced("level.invalid-level",
                        "{file}", file.getPath());
                return;
            }

            // check inconsistent name
            if (!name.equals(level.getName())) {
                MLogger.errorReplaced("level.inconsistent-name",
                        "{level}", level.getName());
                return;
            }

            level.setFile(file);
            this.levels.put(name, level);
            MLogger.infoReplaced("level.load",
                    "{level}", name);
        } catch (IOException e) {
            MLogger.errorReplaced("level.load-error",
                    "{file}", file.getPath());
            Logger.error(e);
        }
    }

    /**
     * Load all levels from the level directory.
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

    /**
     * Reset all the player's level data.
     */
    public void resetAll(@NotNull Player player) {
        requireNonNull(DataManager.getInstance().getPlayerData(player.getUniqueId())).resetAll();
    }

    @Nullable
    public Level getLevel(@NotNull String name) {
        return MapUtils.smartMatch(name, this.levels);
    }

    @NotNull
    public HashMap<String, Level> getLevels() {
        return levels;
    }

    public boolean hasLevel(@NotNull String name) {
        return this.levels.containsKey(name);
    }

    /**
     * Update the player's experience bar.
     *
     * @param player Player
     */
    public void updateExperienceBar(@NotNull Player player) {
        if (experienceBarLevel == null) {
            return;
        }

        if (activatedWorlds.isEmpty() || activatedWorlds.contains(player.getWorld().getName())) {
            var data = experienceBarLevel.getLevelData(player);
            player.setLevel(data.getLevels());
            player.setExp((float) Math.min(data.getPercentage() / 100D, 0.99D));
        }
    }

    /**
     * Start the task updating all players' experience bar. This task runs every 10 seconds.
     */
    public void startExperienceBarTask() {
        if (experienceBarLevel == null) {
            return;
        }

        stopExperienceBarTask();
        experienceBarTask = new ExperienceBarTask();
        experienceBarTask.start();
    }

    /**
     * Cancel the experience bar task if running.
     */
    public void stopExperienceBarTask() {
        if (experienceBarTask != null) {
            experienceBarTask.stop();
        }
    }

    @NotNull
    public static LevelManager getInstance() {
        return DreamLevels.getInstance().getLevelManager();
    }
}
