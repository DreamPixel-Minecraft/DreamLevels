package net.dreampixel.dreamlevels.data.modifier.impl;

import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.modifier.ConfigurationDataModifier;
import net.dreampixel.dreamlevels.data.modifier.StorageMethod;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;

import java.io.File;
import java.util.UUID;

public class JsonDataModifier extends ConfigurationDataModifier {
    public static StorageMethod[] STORAGE_METHODS = { StorageMethod.JSON };

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return ConfigurationProvider.getJsonConfiguration();
    }

    @Override
    public @NotNull File getDataFile(@NotNull UUID uuid) {
        return new File(DataManager.getInstance().getStorageDirectory(),
                uuid + ".json");
    }

    @Override
    public @NotNull StorageMethod[] getStorageMethod() {
        return STORAGE_METHODS;
    }
}
