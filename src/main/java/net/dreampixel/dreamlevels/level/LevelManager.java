package net.dreampixel.dreamlevels.level;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
import net.dreampixel.dreamlevels.task.ExperienceBarTask;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.api.locale.Locale;
import top.shadowpixel.shadowcore.object.interfaces.Manager;
import top.shadowpixel.shadowcore.util.ConfigurationUtils;
import top.shadowpixel.shadowcore.util.collection.MapUtils;
import top.shadowpixel.shadowcore.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A handler class of levels, level events and default level events. <p>
 * (Please distinguish between 'level events' and 'default level events')
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class LevelManager implements Manager {
    private final DreamLevels plugin;

    private final HashMap<String, Level> levels = new HashMap<>();
    private File directory;

    /**
     * Default level events
     */
    private final HashMap<String, LevelEventContainer> defaultLevelEvents = new HashMap<>();
    private ExecutableEvent defaultResetAllEvent;

    /**
     * Experience bar fields
     */
    private ExperienceBarTask experienceBarTask;
    private Level experienceBarLevel;
    private List<String> activatedWorlds;

    public LevelManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public static LevelManager getInstance() {
        return DreamLevels.getInstance().getLevelManager();
    }

    @Override
    public void initialize() {
        // check configuration's validity
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

        // load levels
        loadAll();

        // load default reset-all event
        var events = DreamLevels.getInstance().getDefaultEventsConfiguration();
        defaultResetAllEvent = ExecutableEvent.of(events.getStringList("level-reset-all"));

        // load default level events
        var section = events.getNodeSection("levels");
        if (section == null) {
            return;
        }

        for (var key : section.getKeys()) {
            defaultLevelEvents.put(key, new LevelEventContainer("idk", Objects.requireNonNull(section.getNodeSection(key))));
        }

        // initialize experience bar task
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

                // add empty level data to online players's player data
                DataManager.getInstance().getLoadedData().values().forEach(v ->
                        v.getLevelData().put(name, new LevelData(v.getUniqueId(), name)));

                MLogger.info("level.create");
            } catch (Exception e) {
                MLogger.error("level.create-failed", e);
            }

            // update the menus so the new level system could be properly shown
            LevelSpyManager.getInstance().updateLevelOverallMenu();
            DataSpyManager.getInstance().updateLDOMenus();
        });
    }

    /**
     * Load a level from the level directory with a specific name.
     *
     * @param name      Name
     * @param skipCheck Whether not to check this level is enabled
     *                  in Config.yml when loading.
     */
    public void load(@NotNull String name, boolean skipCheck) {
        // check enablement in configuration
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
            name = name.substring(0, name.lastIndexOf(".")); // cut off the file suffix

            var config = ConfigurationProvider.getYamlConfigurationProvider().load(file);
            var level = (Level) config.get("level");
            if (level == null) {
                MLogger.errorReplaced("level.invalid-level",
                        "{file}", file.getPath());
                return;
            }

            // check the consistency between the file name and the level name
            // to avoid unexpected problems
            if (!name.equals(level.getName())) {
                MLogger.errorReplaced("level.inconsistent-name",
                        "{level}", level.getName());
                return;
            }

            // set the level's storage file, in order to save the level file after
            // modifying the level system
            level.setFile(file);

            this.levels.put(name, level);
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

    /*
     * ------ Level events and containers. ------
     */

    /**
     * @return All default level event containers
     */
    public @NotNull HashMap<String, LevelEventContainer> getDefaultLevelEvents() {
        return defaultLevelEvents;
    }

    /**
     * Get a default level event container.
     *
     * @param level Level name
     * @return Level event container
     */
    public @Nullable LevelEventContainer getDefaultLevelEventContainer(@NotNull String level) {
        return  defaultLevelEvents.get(level);
    }

    /**
     * Get an default event of a level. Return null if the level is missing.
     *
     * @param level Level
     * @param function Function
     * @return Executable event
     */
    public @Nullable ExecutableEvent getDefaultLevelEvent(@NotNull String level, @NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        var eventContainer =  defaultLevelEvents.get(level);
        if (eventContainer == null) {
            eventContainer = defaultLevelEvents.get("_DEFAULT_");
        }

        if (eventContainer == null) {
            return null;
        }

        return function.apply(eventContainer);
    }

    /**
     * Get a level event from a specific locale.
     *
     * <p>If the event cannot be found in the locale,
     * then attempt to retrieve it from the default locale.
     * (should be enabled in the config, otherwise null will be returned)</p>
     *
     * <p>Please note that, whatever the default locale is forcibly used, this method WON'T
     * firstly find the event from the default locale.
     * (This is different from {@link #getLocalizedEvent(Player, String, Function)})</p>
     *
     * @param playerLocale The target locale
     * @param level The level name
     * @param function The function used to get the event
     * @return Executable event, or null if not found
     */
    public @Nullable ExecutableEvent getLocalizedEvent(@NotNull String playerLocale,
                                             @NotNull String level,
                                             @NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        return getLocalizedEvent(
                LocaleUtils.getLocale(playerLocale),
                LocaleUtils.getDefaultLocale(),
                level,
                function
        );
    }

    /**
     * Get a level event from a player's locale. If the event cannot be found in the locale,
     * then attempt to retrieve it from the default locale.
     * (should be enabled in the config, otherwise null will be returned)
     * <p>
     * Please note that this method WILL find the event from the default locale when the
     * default locale is forcibly used.
     * (This is different from {@link #getLocalizedEvent(String, String, Function)} )
     *
     * @param player The target player
     * @param level The level name
     * @param function The function used to get the event
     * @return Executable event, or null if not found
     */
    public @Nullable ExecutableEvent getLocalizedEvent(@NotNull Player player, @NotNull String level, @NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        return getLocalizedEvent(
                LocaleUtils.getNullableLocale(player),
                LocaleUtils.getDefaultLocale(),
                level,
                function
        );
    }

    private @Nullable ExecutableEvent getLocalizedEvent(@Nullable Locale locale,
                                                        @Nullable Locale candidate,
                                                        @NotNull String level,
                                                        @NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        var eventContainer = (LevelEventContainer) null;
        var event = (ExecutableEvent) null;
        if (locale != null) {
            eventContainer = (LevelEventContainer) locale.getProperty("level-" + level.toLowerCase());
            event = function.apply(eventContainer);
        }

        // if the event is null, then turn to one in candidate locale (if enabled in config),
        // or return null else
        if (event == null) {
            if (!DreamLevels.getInstance().getConfiguration().isBoolean("locale.use-candidate-level-events")) {
                return null;
            }
        } else {
            return event;
        }

        if (candidate == null) {
            return null;
        }

        eventContainer = (LevelEventContainer) candidate.getProperty("level-" + level.toLowerCase());
        event = function.apply(eventContainer);
        return event;
    }

    public @NotNull ExecutableEvent getDefaultResetAllEvent() {
        return defaultResetAllEvent;
    }

    /*
     * ------ Experience bar task. ------
     */

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
            // control the max value to 0.99D to avoid leveling up
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
}
