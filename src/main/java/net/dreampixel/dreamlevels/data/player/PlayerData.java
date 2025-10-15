package net.dreampixel.dreamlevels.data.player;

import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.util.collection.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("DreamLevels-Data")
public class PlayerData implements ConfigurationSerializable, IPlayerData {
    private UUID uniqueId;
    private final Map<String, LevelData> levelData;

    public PlayerData(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.levelData = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public PlayerData(@NotNull Map<String, Object> deserialize) {
        this.levelData = (Map<String, LevelData>) deserialize.get("level-data");
    }

    public void setUniqueID(@NotNull UUID uuid) {
        if (this.uniqueId == null) {
            this.uniqueId = uuid;
        }
    }

    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    public @Nullable LevelData getLevelData(@NotNull String level) {
        return MapUtils.smartMatch(level, this.levelData);
    }

    public @NotNull Map<String, LevelData> getLevelData() {
        return levelData;
    }

    public void resetAll() {
        this.levelData.values().forEach(LevelData::reset);
    }

    public void save() {
        DataManager.getInstance().save(this);
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(DreamLevels.getInstance(), this::save);
    }

    public @NotNull Map<String, Object> serialize() {
        return MapUtils.of(
                "level-data", this.levelData
        );
    }
}
