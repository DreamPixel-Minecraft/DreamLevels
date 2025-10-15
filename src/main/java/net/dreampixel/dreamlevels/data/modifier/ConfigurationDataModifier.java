package net.dreampixel.dreamlevels.data.modifier;

import lombok.var;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.Configuration;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;

import java.io.File;
import java.io.Reader;
import java.util.UUID;

@SuppressWarnings ({"unused", "ResultOfMethodCallIgnored"})
public abstract class ConfigurationDataModifier implements FileDataModifier {

    public abstract ConfigurationProvider getConfigurationProvider();

    @Override
    public @Nullable PlayerData load(@NotNull UUID uuid, @NotNull File file) {
        if (!file.exists()) {
            return null;
        }

        try {
            var config = getConfigurationProvider().load(file);
            PlayerData data = getFromConfiguration(config);
            if (data == null) {
                MLogger.error("data.failure.invalid-null");
                return null;
            }

            data.setUniqueID(uuid);
            return data;
        } catch (Throwable e) {
            MLogger.errorReplaced("data.failure.admin.failed-to-load",
                    "{player}", uuid.toString());
            Logger.error(e);
        }

        return null;
    }

    @Override
    public PlayerData load(@NotNull UUID uuid, @NotNull Reader reader) {
        try {
            var config = getConfigurationProvider().load(reader);
            var data = getFromConfiguration(config);
            return data == null ? new PlayerData(uuid) : data;
        } catch (Throwable e) {
            MLogger.errorReplaced("data.failure.admin.failed-to-load",
                    "{player}", uuid.toString());
            Logger.error(e);
        }

        return new PlayerData(uuid);
    }

    @Override
    public @Nullable PlayerData load(@NotNull UUID uuid) {
        return load(uuid, getDataFile(uuid));
    }

    @Override
    public boolean create(@NotNull UUID uuid, @NotNull PlayerData data) {
        var file = getDataFile(uuid);
        if (file.exists()) {
            return false;
        }

        try {
            file.getParentFile().mkdirs();  file.createNewFile();
            save(new PlayerData(uuid));
            return true;
        } catch (Throwable t) {
            MLogger.errorReplaced("data.failure.admin.failed-to-create",
                    "{player}", uuid.toString());
            Logger.error(t);
        }

        return false;
    }

    @Override
    public boolean remove(@NotNull UUID uuid) {
        return getDataFile(uuid).delete();
    }

    @Override
    public boolean save(@NotNull PlayerData data) {
        try {
            var config = new Configuration();
            var file = getDataFile(data.getUniqueId());
            config.set("PlayerData", data);
            getConfigurationProvider().save(config, file);
            return true;
        } catch (Throwable t) {
            MLogger.errorReplaced("data.failure.admin.failed-to-unload",
                    "{player}", data.getUniqueId().toString());
            Logger.error(t);
        }

        return false;
    }

    @Nullable
    public PlayerData getFromConfiguration(@NotNull Configuration config) {
        var data = config.get("PlayerData");
        if (data instanceof PlayerData) {
            return (PlayerData) data;
        }

        return null;
    }
}