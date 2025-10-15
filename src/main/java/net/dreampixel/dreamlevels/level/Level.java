package net.dreampixel.dreamlevels.level;

import lombok.val;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.ILevelData;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.level.OfflineLevelData;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.util.collection.MapUtils;
import top.shadowpixel.shadowcore.util.entity.PlayerUtils;
import top.shadowpixel.shadowcore.util.object.NumberUtils;
import top.shadowpixel.shadowcore.util.text.ColorUtils;
import top.shadowpixel.shadowcore.util.text.StringUtils;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
@SerializableAs("DreamLevels-Level")
public class Level implements ConfigurationSerializable {
    /**
     * basic vars for a level system
     */
    private final String name;
    private final String displayName;

    private int maxLevels = 100;
    private int defaultLevel = 0;
    private int defaultRequiredExp = 5000;

    private final Map<Integer, Integer> expToLevel = new HashMap<>();
    private final LinkedHashMap<Integer, String> colors = new LinkedHashMap<>();

    private final HashMap<String, HashMap<String, ExecutableEvent>> levelEvents = new HashMap<>();

    /**
     * Create an empty level.
     *
     * @param name Name
     */
    public Level(String name) {
        this.name = name;
        this.displayName = name;

        loadEvents();
    }

    /**
     * Deserialization constructor.
     */
    public Level(@NotNull Map<String, Object> deserialize) {
        this.name = (String) deserialize.get("name");
        this.displayName = (String) deserialize.get("display-name");

        this.maxLevels = (int) deserialize.get("max-levels");
        this.defaultLevel = (int) deserialize.get("default-level");
        this.defaultRequiredExp = (int) deserialize.get("default-exp-to-level-up");

        this.expToLevel.putAll((HashMap<Integer, Integer>) deserialize.get("custom-exp-to-level-up"));
        this.colors.putAll((LinkedHashMap<Integer, String>) deserialize.get("colors"));

        loadEvents();
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

    /**
     * @return Default max levels of this level
     */
    public int getDefaultMaxLevels() {
        return maxLevels;
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
     * @return Default level
     */
    public int getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * @return The default amount of exp to level up
     */
    public int getDefaultRequiredExp() {
        return defaultRequiredExp;
    }

    /**
     * @param nextLevel Next level
     * @return The exp required leveling up from the previous level to this level. </br>
     *         Returns -1 if {@code nextLevel} >= the max level
     */
    public int getRequiredExp(int nextLevel) {
        if (nextLevel > maxLevels) {
            return -1;
        }

        if (expToLevel.containsKey(nextLevel)) {
            return expToLevel.get(nextLevel);
        }

        int exp = getDefaultRequiredExp();
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
        return level >= this.maxLevels;
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

    @NotNull
    public LevelData getLevelData(@NotNull Player player) {
        var data = DataManager.getInstance().getPlayerData(player.getUniqueId());
        assert data != null;

        var levelData = data.getLevelData(this.name);
        if (levelData == null) {
            levelData = new LevelData(player.getUniqueId(), this.name);
        }

        return levelData;
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

    public void loadEvents() {
        var plugin = DreamLevels.getInstance();
        plugin.getLocaleManager().getLocales().forEach((name, locale) -> {
            var events = locale.getConfig("Events");
            if (events == null) {
                return;
            }

            val section = events.isNodeSection("levels." + this.name) ?
                    events.getConfigurationSection("levels." + this.name) :
                    events.getConfigurationSection("levels._DEFAULT_");

            if (section == null) {
                Logger.warn("invalid level events for " + name);
                return;
            }

            var keys = new HashSet<>(section.getKeys());
            var loadedEvents = new HashMap<String, ExecutableEvent>();

            // load level-up events
            var level_up = section.getConfigurationSection("level-up-events");
            if (level_up != null) {
                keys.remove("level-up-events"); // remove this node for avoiding duplicated loading
                level_up.getKeys().forEach(key -> {
                    var event = ExecutableEvent.of(level_up.getStringList(key));
                    event.replacePermanently("{prefix}", DreamLevels.getPrefix());
                    if (StringUtils.isInteger(key)) {
                        event.replacePermanently("{levels}", key);
                    }

                    loadedEvents.put("LEVEL_UP_" + key.toUpperCase(), event);
                });
            }

            keys.forEach(key -> {
                if (key.equals("levels-added")) {
                    var list = section.getStringList("levels-added");
                    if (!list.isEmpty() && list.get(0).equalsIgnoreCase("LEVEL_UP")) {
                        return;
                    }
                }

                var event = ExecutableEvent.of(section.getStringList(key));
                event.replacePermanently("{prefix}", DreamLevels.getPrefix());
                loadedEvents.put(key, event);
            });

            this.levelEvents.put(name, loadedEvents);
        });
    }

    /**
     * Get a leveling up event according to player's locale.
     *
     * @return Leveling up event
     */
    @Nullable
    public ExecutableEvent getLevelEvent(@NotNull Player player, @NotNull String key) {
        var locale = PlayerUtils.getLocale(player);
        if (key.equalsIgnoreCase("levels-added") && !this.levelEvents.containsKey("levels-added")) {
            return getLevelUpEvent(player, getLevelData(player).getLevels());
        }

        var events = MapUtils.smartMatch(locale, this.levelEvents);
        if (events == null) {
            return ExecutableEvent.emptyEvent();
        }

        return events.get(key);
    }

    @Nullable
    public ExecutableEvent getLevelUpEvent(@NotNull Player player, int level) {
        var event = getLevelEvent(player, "LEVEL_UP_" + level);
        if (event == null) {
            event = getLevelEvent(player, "LEVEL_UP_DEFAULT");
        }

        return event;
    }

    @Nullable
    public ExecutableEvent getLocalizedLevelEvent(@NotNull String locale, @NotNull String key) {
        var events = this.levelEvents.get(locale);
        if (events == null) {
            return null;
        }

        return events.get(key);
    }

    @Nullable
    public ExecutableEvent getLocalizedLevelUpEvent(@NotNull String locale, int level) {
        var event = getLocalizedLevelEvent(locale, "LEVEL_UP_" + level);
        if (event == null) {
            event = getLocalizedLevelEvent(locale, "LEVEL_UP_DEFAULT");
        }

        return event;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return MapUtils.of(
                "name", this.name,
                "display-name", this.displayName,
                "max-levels", this.maxLevels,
                "default-level", this.defaultLevel,
                "default-exp-to-level-up", this.defaultRequiredExp,
                "custom-exp-to-level-up", this.expToLevel,
                "colors", this.colors
        );
    }
}
