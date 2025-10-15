package net.dreampixel.dreamlevels.data.modifier.impl;

import lombok.Cleanup;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.data.modifier.DataModifier;
import net.dreampixel.dreamlevels.data.modifier.StorageMethod;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.impl.json.JsonSerialization;
import top.shadowpixel.shadowcore.util.sql.SQLUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class SQLDataModifier implements DataModifier {
    public static StorageMethod[] STORAGE_METHODS = { StorageMethod.SQLITE, StorageMethod.MYSQL };

    public Connection getConnection() {
        return Objects.requireNonNull(DataManager.getInstance().getDatabase()).getConnection();
    }

    @Override
    public @Nullable PlayerData load(@NotNull UUID uuid) {
        try {
            @Cleanup var conn = getConnection();
            @Cleanup var pstmt = SQLUtils.prepareStmt(conn,
                    "SELECT * FROM " + getTable() + " WHERE UUID=?",
                    uuid.toString());
            @Cleanup var resultSet = pstmt.executeQuery();
            if (!resultSet.next()) {
                MLogger.error("data.failure.invalid-null");
                return null;
            }

            //Set UUID
            PlayerData data = JsonSerialization.deserializeJson(resultSet.getString("Data"), PlayerData.class);
            if (data == null) {
                MLogger.error("data.failure.invalid-null");
                return null;
            }

            data.setUniqueID(uuid);
            return data;
        } catch (SQLException e) {
            MLogger.errorReplaced("data.failure.admin.failed-to-load",
                    "{player}", uuid.toString());
            Logger.error(e);
        }

        return null;
    }

    @Override
    public boolean create(@NotNull UUID uuid, @NotNull PlayerData data) {
        try {
            return SQLUtils.executeUpdate(getConnection(),
                    "INSERT INTO " + getTable() + " VALUES (?, ?)",
                    uuid.toString(), serialize(data)) > 0;
        } catch (Throwable e) {
            Logger.error("An error occurred while creating data.", e);
            return false;
        }
    }

    @Override
    public boolean remove(@NotNull UUID uuid) {
        try {
            @Cleanup var ps = SQLUtils.prepareStmt(getConnection(),
                    "DELETE FROM " + getTable() + " WHERE UUID=?",
                    uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (Throwable e) {
            Logger.error("An error occurred while deleting data for " + uuid, e);
            return false;
        }
    }

    @Override
    public boolean save(@NotNull PlayerData data) {
        try {
            SQLUtils.executeUpdate(getConnection(),
                    "UPDATE " + getTable() + " SET Data=? WHERE UUID=?",
                    serialize(data), data.getUniqueId().toString());
            return true;
        } catch (Throwable e) {
            Logger.error("An error occurred while saving data for " + data.getUniqueId(), e);
            return false;
        }
    }

    @Override
    public boolean exist(@NotNull UUID uuid) {
        try {
            @Cleanup var pstmt = SQLUtils.prepareStmt(getConnection(),
                    "SELECT * FROM " + getTable() + " WHERE UUID=?",
                    uuid.toString());
            @Cleanup var set = pstmt.executeQuery();
            return set.next();
        } catch (Throwable e) {
            Logger.error("An error occurred while querying from SQL", e);
            return false;
        }
    }

    @Override
    public @NotNull StorageMethod[] getStorageMethod() {
        return STORAGE_METHODS;
    }

    @NotNull
    private String getTable() {
        var plugin = DreamLevels.getInstance();
        switch (DataManager.getInstance().getStorageMethod()) {
            case MYSQL:
                return plugin.getConfiguration().getString("data.MySQL.table");
            case SQLITE:
                return plugin.getConfiguration().getString("data.SQLite.table");
        }

        return "DreamLevels_db_error";
    }

    private String serialize(@NotNull PlayerData data) {
        return JsonSerialization.serializeToJson(data, false);
    }
}
