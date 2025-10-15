package net.dreampixel.dreamlevels.data;

import lombok.Cleanup;
import lombok.Getter;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.level.OfflineLevelData;
import net.dreampixel.dreamlevels.data.modifier.DataModifier;
import net.dreampixel.dreamlevels.data.modifier.StorageMethod;
import net.dreampixel.dreamlevels.data.modifier.impl.JsonDataModifier;
import net.dreampixel.dreamlevels.data.modifier.impl.SQLDataModifier;
import net.dreampixel.dreamlevels.data.modifier.impl.YamlDataModifier;
import net.dreampixel.dreamlevels.data.level.NullOfflineLevelData;
import net.dreampixel.dreamlevels.data.player.IPlayerData;
import net.dreampixel.dreamlevels.data.player.NullOfflinePlayerData;
import net.dreampixel.dreamlevels.data.player.OfflinePlayerData;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.database.SQLDatabase;
import top.shadowpixel.shadowcore.api.database.file.SQLiteDatabase;
import top.shadowpixel.shadowcore.api.database.hikari.MySQLDatabase;
import top.shadowpixel.shadowcore.api.uid.UUIDStorage;
import top.shadowpixel.shadowcore.api.util.Optional;
import top.shadowpixel.shadowcore.object.interfaces.Manager;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings ({"UnusedReturnValue", "unused"})
public class DataManager implements Manager {
    private final DreamLevels plugin;

    @Getter
    private StorageMethod storageMethod;
    @Getter
    private File storageDirectory;
    @Getter
    private DataModifier dataModifier;

    @Getter
    private SQLDatabase database;

    private final HashMap<UUID, PlayerData> loadedData = new HashMap<>(0);

    public DataManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings ("ResultOfMethodCallIgnored")
    @Override
    public void initialize() {
        var config = plugin.getConfiguration();
        this.storageDirectory = new File(config.getString("data.storage-directory")
                .replace("{default}", plugin.getDataFolder().toString()));

        // storage method
        try {
            storageMethod = StorageMethod.valueOf(config.getString("data.storage-method").toUpperCase());
        } catch (IllegalArgumentException e) {
            storageMethod = StorageMethod.YAML;
            MLogger.errorReplaced("data.failure.invalid-storage-method",
                    "{method}", config.getString("data.storage-method"));
        }

        this.dataModifier = initDataModifier();
        if (isDatabaseMode()) {
            initDatabase();
        } else {
            // create and check PlayerData folders
            storageDirectory = new File(config.getString("data.storage-directory", "")
                    .replace("{default}", plugin.getDataFolder().toString()));
            storageDirectory.mkdirs();
            if (!storageDirectory.isDirectory()) {
                storageDirectory = new File(plugin.getDataFolder(), "PlayerData");
                MLogger.errorReplaced("data.failure.invalid-storage-directory");
                return;
            }

            loadOnline();
        }
    }

    @Override
    public void unload() {
        this.loadedData.values().forEach(this::save);
        this.loadedData.clear();

        if (database != null && database.isInitialized()) {
            database.close();
        }
    }

    /**
     * Create data if not exist.
     *
     * @param uuid Player's uuid
     * @return Whether created or not
     */
    public boolean create(UUID uuid) {
        var data = complete(new PlayerData(uuid));
        this.loadedData.put(uuid, data);
        return this.dataModifier.create(uuid, data);
    }

    /**
     * Load a player data.
     *
     * @param uuid Player uuid
     */
    public void load(@NotNull UUID uuid) {
        load(uuid, false);
    }

    public void load(@NotNull UUID uuid, boolean create) {
        var data = dataModifier.load(uuid);
        if (data != null) {
            this.loadedData.put(uuid, complete(data));
        } else if (create && !exist(uuid)) {
            create(uuid);
        }
    }

    /**
     * Load all online players' data.
     */
    public void loadOnline() {
        Bukkit.getOnlinePlayers().forEach(p -> load(p.getUniqueId(), true));
    }

    /**
     * Unload and save data.
     *
     * @param uuid Player uuid
     * @return Whether unloaded or not
     */
    public boolean unload(UUID uuid) {
        return unload(uuid, true);
    }

    /**
     * Unload data. <p>
     * If {@param save} is true, this data will be saved or dropped.
     *
     * @param uuid Player uuid
     * @param save Whether saving
     * @return Whether saved or not
     */
    public boolean unload(UUID uuid, boolean save) {
        if (save) {
            var data = getPlayerData(uuid);
            save(data);
        }

        return this.loadedData.remove(uuid) != null;
    }

    /**
     * Remove a data permanently.
     *
     * @param uuid Player data
     * @return Whether removed or not
     */
    public boolean remove(UUID uuid) {
        this.loadedData.remove(uuid);
        return this.dataModifier.remove(uuid);
    }

    /**
     * Return whether specific data with this uuid is loaded.
     *
     * @param uuid Player uuid
     * @return Whether specific data with this uuid is loaded
     */
    public boolean isLoaded(UUID uuid) {
        return this.loadedData.containsKey(uuid);
    }

    /**
     * Return whether specific data with this uuid exists
     *
     * @param uuid Player uuid
     * @return Whether specific data with this uuid exists
     */
    public boolean exist(@NotNull UUID uuid) {
        return this.dataModifier.exist(uuid);
    }

    /**
     * Save data without unloading.
     *
     * @param uuid Player uuid
     * @return Whether saved successfully or not.
     */
    public boolean save(@NotNull UUID uuid) {
        var data = getPlayerData(uuid);
        return save(data);
    }

    /**
     * Save a player's data.
     *
     * @param data Data to save
     * @return Whether saved successfully or not
     */
    public boolean save(PlayerData data) {
        if (data == null) {
            return false;
        }

        return this.dataModifier.save(data);
    }

    /**
     * Save all online players' data.
     */
    public void saveOnline() {
        Bukkit.getOnlinePlayers().forEach(t -> save(t.getUniqueId()));
    }

    /**
     * Get data with specific uuid.
     *
     * @param uuid Player uuid
     * @return Player data
     */
    @Nullable
    public PlayerData getPlayerData(@NotNull UUID uuid) {
        return this.loadedData.get(uuid);
    }

    @NotNull
    public PlayerData getPlayerData(@NotNull Player player) {
        return Objects.requireNonNull(this.loadedData.get(player.getUniqueId()));
    }

    @Nullable
    public PlayerData loadOfflineData(@NotNull UUID uuid) {
        return getDataModifier().load(uuid);
    }

    @Nullable
    public PlayerData loadOfflineData(@NotNull String playerName) {
        return Optional.of(UUIDStorage.getUniqueID(playerName))
                .ret(s -> getDataModifier().load(s));
    }

    @NotNull
    public OfflinePlayerData getOfflineDataModifier(@NotNull String playerName, @NotNull Consumer<String> feedbackConsumer) {
        var uniqueId = UUIDStorage.getUniqueID(playerName);
        return uniqueId == null ? NullOfflinePlayerData.of(feedbackConsumer) : getOfflineDataModifier(uniqueId, feedbackConsumer);
    }

    @NotNull
    public OfflinePlayerData getOfflineDataModifier(@NotNull UUID uniqueId, @NotNull Consumer<String> feedbackConsumer) {
        return new OfflinePlayerData(uniqueId, feedbackConsumer);
    }

    @NotNull
    public OfflineLevelData getOfflineLevelDataModifier(@NotNull String playerName, @NotNull String level, @NotNull Consumer<String> feedbackConsumer) {
        var uniqueId = UUIDStorage.getUniqueID(playerName);
        return uniqueId == null ? NullOfflineLevelData.of(feedbackConsumer) : getOfflineLevelDataModifier(uniqueId, level, feedbackConsumer);
    }

    @NotNull
    public OfflineLevelData getOfflineLevelDataModifier(@NotNull UUID uniqueId, @NotNull String level, @NotNull Consumer<String> feedbackConsumer) {
        return new OfflineLevelData(uniqueId, feedbackConsumer, level);
    }

    /**
     * Get a modifiable data for a player. </br>
     * Returns a {@link PlayerData} instance if the player is online,
     * otherwise, returns an {@link OfflinePlayerData} instance.
     *
     * @param uniqueId Unique ID
     * @return Modifiable Player Data
     */
    @NotNull
    public IPlayerData getAdaptivePlayerData(@NotNull UUID uniqueId, @NotNull Consumer<String> onOfflineFeedback) {
        var data = getPlayerData(uniqueId);
        if (data != null) {
            return data;
        }

        return new OfflinePlayerData(uniqueId, onOfflineFeedback);
    }

    /**
     * Get loaded data.
     *
     * @return Loaded data
     */
    @NotNull
    public Map<UUID, PlayerData> getLoadedData() {
        return Collections.unmodifiableMap(loadedData);
    }

    public boolean isDatabaseMode() {
        return storageMethod == StorageMethod.SQLITE ||
               storageMethod == StorageMethod.MYSQL;
    }

    public void initDatabase() {
        switch (storageMethod) {
            case MYSQL:
                var mysql = plugin.getConfiguration().getConfigurationSection("data.MySQL");
                if (mysql == null) {
                    MLogger.errorReplaced("data.failure.invalid-MySQL-configuration");
                    return;
                }

                var mysqlTable = mysql.getString("table");
                var mysqlDb = new MySQLDatabase(plugin) {
                    @Override
                    public void onConnected() {
                        createTable(getConnection(), mysqlTable);
                        loadOnline();
                    }
                };

                this.database = mysqlDb;
                mysqlDb.initialize(mysql.getString("host"),
                        mysql.getString("port"),
                        mysql.getString("database"),
                        mysql.getString("username"),
                        mysql.getString("password"));
                break;
            case SQLITE:
                var sqlite = plugin.getConfiguration().getConfigurationSection("data.SQLite");
                if (sqlite == null) {
                    MLogger.errorReplaced("data.failure.invalid-SQLite-configuration");
                    return;
                }

                var file = new File(sqlite.getString("file")
                        .replace("{default}", plugin.getDataFolder().toString()));
                var sqliteTable = sqlite.getString("table");
                var sqliteDb = new SQLiteDatabase(plugin) {
                    @Override
                    public void onConnected() {
                        createTable(getConnection(), sqliteTable);
                        loadOnline();
                    }
                };

                this.database = sqliteDb;
                sqliteDb.initialize(file);
                break;
        }
    }

    private void createTable(@NotNull Connection connection, @NotNull String table) {
        try {
            @Cleanup var stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + "  (UUID VARCHAR(50) PRIMARY KEY , Data TEXT)");
        } catch (SQLException e) {
            Logger.error("An error occurred while creating table.", e);
        }
    }

    @NotNull
    private DataModifier initDataModifier() {
        switch (storageMethod) {
            case YAML:
                return new YamlDataModifier();
            case JSON:
                return new JsonDataModifier();
            case MYSQL:
            case SQLITE:
                return new SQLDataModifier();
        }

        throw new IllegalArgumentException("unknown DataModifier");
    }

    /**
     * Complete level data.
     */
    private PlayerData complete(@NotNull PlayerData data) {
        LevelManager.getInstance().getLevels().keySet().forEach(l -> {
            if (!data.getLevelData().containsKey(l)) {
                data.getLevelData().put(l, new LevelData(data.getUniqueId(), l));
            }
        });

        data.getLevelData().forEach((k, v) -> {
            v.setLevel(k);
            v.setUniqueId(data.getUniqueId());
        });

        return data;
    }

    @NotNull
    public static DataManager getInstance() {
        return DreamLevels.getInstance().getDataManager();
    }
}
