package net.dreampixel.dreamlevels.level;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.ILevelData;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.level.OfflineLevelData;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.Configuration;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.util.collection.MapUtils;
import top.shadowpixel.shadowcore.util.entity.PlayerUtils;
import top.shadowpixel.shadowcore.util.object.NumberUtils;
import top.shadowpixel.shadowcore.util.text.ColorUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "unused"})
@SerializableAs("DreamLevels-Level")
public class Level implements ConfigurationSerializable {
    /**
     * basic vars for a level system
     */
    private final String name;
    private final Map<Integer, Integer> expToLevel = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> colors = new LinkedHashMap<>();

    private String displayName;

    private int defaultMaxLevels = 100;
    private int defaultLevels = 0;
    private double defaultRequiredExp = 5000D;

    private File storageFile;

    /**
     * Create an empty level.
     *
     * @param name Name
     */
    public Level(String name) {
        this.name = name;
        this.displayName = name;
    }

    /**
     * Deserialization constructor.
     */
    public Level(@NotNull Map<String, Object> deserialize) {
        this.name = (String) deserialize.get("name");
        this.displayName = (String) deserialize.get("display-name");

        this.defaultMaxLevels = (int) deserialize.get("max-levels");
        this.defaultLevels = (int) deserialize.get("default-level");
        this.defaultRequiredExp = (double) deserialize.get("default-exp-to-level-up");

        this.expToLevel.putAll((HashMap<Integer, Integer>) deserialize.get("custom-exp-to-level-up"));
        this.colors.putAll((LinkedHashMap<Integer, String>) deserialize.get("colors"));
    }

    /**
     * @return Level name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * @return Level display name
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
        postprocess();
    }

    /**
     * @return Default max levels of this level
     */
    public int getDefaultMaxLevels() {
        return defaultMaxLevels;
    }

    /**
     * Set the default max levels.
     *
     * @param levels Default max levels
     */
    public void setDefaultMaxLevels(int levels) {
        this.defaultMaxLevels = levels;
        postprocess();
    }

    /**
     * @param player Player
     * @return The max levels of a player
     */
    public int getMaxLevels(Player player) {
        var node = ("DreamLevels.Levels." + this.name + ".MaxLevels.").toLowerCase();
        // find effective permission that decided the max levels
        var max = player.getEffectivePermissions().stream()
                .filter(p -> p.getPermission().toLowerCase().startsWith(node))
                .mapToInt(s -> NumberUtils.parseInt(s.getPermission().substring(node.length()), -1))
                .max();
        return (max.isPresent() && max.getAsInt() > -1) ? max.getAsInt() : getDefaultMaxLevels();
    }

    /**
     * @return The default levels that a new level data starts from
     */
    public int getDefaultLevels() {
        return defaultLevels;
    }

    /**
     * Set the default levels that a new level data starts from.
     *
     * @param defaultLevels The default levels
     */
    public void setDefaultLevels(int defaultLevels) {
        this.defaultLevels = defaultLevels;
        postprocess();
    }

    /**
     * @return The default amount of exp to level up
     */
    public double getDefaultRequiredExp() {
        return defaultRequiredExp;
    }

    /**
     * Set the default exp to level up. This is only used when a level has no
     * specified exp amount to level up.
     *
     * @param defaultRequiredExp Default exp to level up
     */
    public void setDefaultRequiredExp(double defaultRequiredExp) {
        this.defaultRequiredExp = defaultRequiredExp;
        postprocess();
    }

    /**
     * @param nextLevel Next level
     * @return The exp required leveling up from the previous level to this level. </br>
     * Returns -1 if {@code nextLevel} >= the max level
     */
    public double getRequiredExp(int nextLevel) {
        if (nextLevel > defaultMaxLevels) {
            return -1;
        }

        if (expToLevel.containsKey(nextLevel)) {
            return expToLevel.get(nextLevel);
        }

        double exp = getDefaultRequiredExp();
        for (var entry : expToLevel.entrySet()) {
            if (entry.getKey() >= nextLevel) {
                exp = entry.getValue();
                break;
            }
        }

        return exp;
    }

    /**
     * @param level Level
     * @return Whether the level has reached the max
     */
    public boolean isMax(int level) {
        return level >= this.defaultMaxLevels;
    }

    @NotNull
    public Map<Integer, Integer> getExpToLevel() {
        return expToLevel;
    }

    /**
     * Get the color of the current level.
     */
    @NotNull
    public String getColor(int level) {
        if (colors.containsKey(level)) {
            return colors.get(level);
        }

        var color = "";
        for (var entry : colors.entrySet()) {
            var level1 = entry.getKey();
            var color1 = entry.getValue();
            if (level1 > level) {
                break;
            } else {
                color = color1;
            }
        }

        return ColorUtils.colorize(color);
    }

    @Nullable
    public LevelData getLevelData(@NotNull UUID uniqueId) {
        var data = DataManager.getInstance().getPlayerData(uniqueId);
        assert data != null;

        var levelData = data.getLevelData(this.name);
        if (levelData == null) {
            levelData = new LevelData(uniqueId, this.name);
        }

        return levelData;
    }

    @NotNull
    public LevelData getLevelData(@NotNull Player player) {
        return Objects.requireNonNull(getLevelData(player.getUniqueId()));
    }

    /**
     * Get a modifiable level data for a player. </br>
     * Returns a {@link LevelData} instance if the player is online,
     * otherwise, returns an {@link OfflineLevelData} instance.
     *
     * @param uniqueId Unique ID
     * @return Modifiable Player Data
     */
    @NotNull
    public ILevelData getAdaptiveLevelData(@NotNull UUID uniqueId, @NotNull Consumer<String> feedbackConsumer) {
        var player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            return getLevelData(player);
        }

        return new OfflineLevelData(uniqueId, feedbackConsumer, this.name);
    }

    /**
     * @return Colors
     */
    @NotNull
    public LinkedHashMap<Integer, String> getColors() {
        return colors;
    }

    /**
     * Get a level event from a specific locale. If the event cannot be found in the locale,
     * then trying to get it from the default locale. (should be enabled in the config,
     * return null else)
     *
     * @param playerLocale Locale
     * @param function Geting-event function
     * @return Executable event
     */
    public @Nullable ExecutableEvent getLocalizedEvent(@NotNull String playerLocale, @NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        return LevelManager.getInstance().getLocalizedEvent(playerLocale, getName(), function);
    }

    /**
     * Get a level event from a player's locale. If the event cannot be found in  the locale,
     * then trying to get it from the default locale. (should be enabled in the config,
     * return null else)
     *
     * @param player Player
     * @param function Getting-event function
     * @return Executable event
     */
    public @Nullable ExecutableEvent getLocalizedEvent(@NotNull Player player, @NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        return getLocalizedEvent(PlayerUtils.getLocale(player), function);
    }

    /**
     * Get a default of this level. Return null if this level has no default event.
     *
     * @param function The function used to get event
     * @return Executable event, or null if missing
     */
    public @Nullable ExecutableEvent getDefaultEvent(@NotNull Function<LevelEventContainer, ExecutableEvent> function) {
        return LevelManager.getInstance().getDefaultLevelEvent(getName(), function);
    }

    /**
     * Get level events from a specific locale. If the locale cannot be found or
     * there is no specific events in this locale, then null will be returned.
     *
     * @param locale Locale name
     * @return Level event container
     */
    @Nullable
    public LevelEventContainer getLevelEvents(@NotNull String locale) {
        var locale1 = LocaleManager.getInstance().getLocale(locale);
        if (locale1 == null) {
            return null;
        }

        return (LevelEventContainer) locale1.getProperty("level-" + getName().toLowerCase());
    }

    /**
     * Post-processing steps after this level was modified, including update the specific
     * level-spy menu and save the level to its file.
     */
    protected void postprocess() {
        // update menu items for level spy menu
        LevelSpyManager.getInstance().updateMenu(this);

        // save to the storage file
        if (storageFile == null) {
            return;
        }

        try {
            // create configuration
            var config = new Configuration();
            config.set("level", this);
            // save
            ConfigurationProvider.getYamlConfigurationProvider().save(config, storageFile);
        } catch (IOException e) {
            MLogger.errorReplaced("level.save-failed",
                    "{file}", storageFile.toString());
        }
    }

    protected void setFile(@NotNull File file) {
        if (storageFile == null) {
            storageFile = file;
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return MapUtils.of(
                "name", this.name,
                "display-name", this.displayName,
                "max-levels", this.defaultMaxLevels,
                "default-level", this.defaultLevels,
                "default-exp-to-level-up", this.defaultRequiredExp,
                "custom-exp-to-level-up", this.expToLevel,
                "colors", this.colors
        );
    }
}
