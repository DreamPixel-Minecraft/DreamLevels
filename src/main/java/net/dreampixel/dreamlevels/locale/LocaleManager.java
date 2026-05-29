package net.dreampixel.dreamlevels.locale;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.LevelEventContainer;
import net.dreampixel.dreamlevels.util.Logger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.ConfigurationProvider;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.api.locale.AbstractLocaleManager;
import top.shadowpixel.shadowcore.api.locale.Locale;
import top.shadowpixel.shadowcore.api.locale.LocaleInfo;
import top.shadowpixel.shadowcore.util.collection.ListUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class LocaleManager extends AbstractLocaleManager<DreamLevels> {
    public static final List<LocaleInfo> PRESET_LOCALE_INFOS = ListUtils.immutableList(
            LocaleInfo.builder()
                    .name("zh_CN")
                    .internalPath("Locale/zh_CN")
                    .complete()
                    .uncompletableFile("Events.yml")
                    .build()
    );
    public static final List<String> CONTENTS = ListUtils.immutableList(
            "Message.yml",
            "Events.yml"
    );
    private static Locale internal;

    public LocaleManager(DreamLevels plugin, File directory) {
        super(plugin, directory);
    }

    @NotNull
    public static LocaleManager getInstance() {
        return DreamLevels.getInstance().getLocaleManager();
    }

    @NotNull
    public static Locale getInternal() {
        if (internal == null) {
            var plugin = DreamLevels.getInstance();

            // create internal locale
            var locale = new Locale(plugin, "Internal", null, CONTENTS);
            locale.setName("Internal");

            // add configuration
            try {
                var yaml = ConfigurationProvider.getYamlConfigurationProvider();
                locale.addConfig("Items", yaml.load(plugin.getResource("Items.yml")));
                locale.addConfig("Default-events", yaml.load(plugin.getResource("Default-events.yml")));
                locale.addConfig("Message", yaml.load(plugin.getResource("Locale/zh_CN/Message.yml")));
                locale.addConfig("Events", yaml.load(plugin.getResource("Locale/zh_CN/Events.yml")));
            } catch (Exception e) {
                Logger.warn(
                        "Failed to load internal locale!",
                        "It may not affect a lot, which can probably be ignored."
                );
            }

            internal = locale;
        }

        return internal;
    }

    @Override
    public void initialize() {
        super.initialize();
        setDefaultLocale(plugin.getConfiguration().getString("locale.default-locale"));
        loadLevelEvents();
    }

    /**
     * Load level events for every locale.
     */
    public void loadLevelEvents() {
        // add all events to property
        getLocales().values().forEach(locale -> {
            var events = locale.getConfig("Events");
            if (events == null) {
                return;
            }

            // load level-reset-all event
            var event = ExecutableEvent.of(events.getStringList("level-reset-all"));
            event.replacePermanently("{prefix}", DreamLevels.getPrefix());
            locale.setProperty("level-reset-all", event);

            // load events of every levels as level event container objects,
            // then put them to the property
            var levelsSection = events.getNodeSection("levels");
            if (levelsSection == null) {
                return;
            }

            for (var key : levelsSection.getKeys()) {
                var levelSection = levelsSection.getNodeSection(key);
                if (levelSection == null) {
                    continue;
                }

                var eventsContainer = new LevelEventContainer(locale.getName(), levelSection);
                locale.setProperty("level-" + key.toLowerCase(), eventsContainer);
            }
        });
    }

    @Override
    public @NotNull Collection<LocaleInfo> getLocaleInfos() {
        return PRESET_LOCALE_INFOS;
    }

    @Override
    public @NotNull Collection<String> getContents() {
        return CONTENTS;
    }
}
