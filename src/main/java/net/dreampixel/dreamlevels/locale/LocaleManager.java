package net.dreampixel.dreamlevels.locale;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.util.Logger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;
import top.shadowpixel.shadowcore.api.locale.AbstractLocaleManager;
import top.shadowpixel.shadowcore.api.locale.Locale;
import top.shadowpixel.shadowcore.api.locale.LocaleInfo;
import top.shadowpixel.shadowcore.util.collection.ListUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class LocaleManager extends AbstractLocaleManager<DreamLevels> {
    public static final List<LocaleInfo> PRESET_LOCALE_INFOS = ListUtils.immutableList(
            LocaleInfo.of("zh_CN", "Locale/zh_CN"),
            LocaleInfo.of("en_US", "Locale/en_US")
    );

    public static final List<String> CONTENTS = ListUtils.immutableList(
            "Message.yml",
            "Events.yml"
    );

    public LocaleManager(DreamLevels plugin, File directory) {
        super(plugin, directory);
    }

    @Override
    public void initialize() {
        super.initialize();
        setDefaultLocale(plugin.getConfiguration().getString("locale.default-locale"));
    }

    @Override
    public @NotNull Collection<LocaleInfo> getLocaleInfos() {
        return PRESET_LOCALE_INFOS;
    }

    @Override
    public @NotNull Collection<String> getContents() {
        return CONTENTS;
    }

    @NotNull
    public static LocaleManager getInstance() {
        return DreamLevels.getInstance().getLocaleManager();
    }
    private static Locale internal;

    @NotNull
    public static Locale getInternal() {
        if (internal == null) {
            var plugin = DreamLevels.getInstance();
            // create internal locale
            var locale = new Locale(plugin, "Internal", null, CONTENTS);
            locale.setName("Internal");

            // add configuration
            try {
                locale.addConfig("Message", ConfigurationProvider.getYamlConfigurationProvider()
                        .load(plugin.getResource("Locale/zh_CN/Message.yml")));
            } catch (Exception e) {
                Logger.error(
                        "Failed to load internal locale!",
                        "It may not affect a lot, you can ignore it."
                );
            }

            internal = locale;
        }

        return internal;
    }
}
