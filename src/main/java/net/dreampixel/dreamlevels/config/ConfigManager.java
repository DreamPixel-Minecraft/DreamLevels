package net.dreampixel.dreamlevels.config;

import net.dreampixel.dreamlevels.DreamLevels;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.manager.AbstractConfigManager;

public class ConfigManager extends AbstractConfigManager {
    public ConfigManager(@NotNull DreamLevels plugin) {
        super(plugin);
        addProperty()
                .identity("Config")
                .resourceName("Config.yml")
                .finish();
        addProperty()
                .identity("Items")
                .resourceName("Items.yml")
                .finish();
    }

    @NotNull
    public static ConfigManager getInstance() {
        return DreamLevels.getInstance().getConfigManager();
    }
}
