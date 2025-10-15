package net.dreampixel.dreamlevels.data.modifier.impl;

import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.modifier.ConfigurationDataModifier;
import net.dreampixel.dreamlevels.data.modifier.StorageMethod;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;

import java.io.File;
import java.util.UUID;

public class YamlDataModifier extends ConfigurationDataModifier {
    public static StorageMethod[] STORAGE_METHODS = { StorageMethod.YAML };

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return ConfigurationProvider.getYamlConfigurationProvider();
    }

    @Override
    public @NotNull File getDataFile(@NotNull UUID uuid) {
        return new File(DataManager.getInstance().getStorageDirectory(),
                uuid + ".yml");
    }

    @Override
    public @NotNull StorageMethod[] getStorageMethod() {
        return STORAGE_METHODS;
    }
}
