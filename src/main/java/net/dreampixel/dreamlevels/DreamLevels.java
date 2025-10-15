package net.dreampixel.dreamlevels;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.listener.DataListener;
import net.dreampixel.dreamlevels.listener.LevelListener;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.sync.SyncManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.exception.ParameterizedCommandInterruptedException;
import top.shadowpixel.shadowcore.api.config.component.Configuration;
import top.shadowpixel.shadowcore.api.config.component.FiledConfiguration;
import top.shadowpixel.shadowcore.api.locale.Locale;
import top.shadowpixel.shadowcore.api.plugin.AbstractPlugin;
import top.shadowpixel.shadowcore.api.util.time.MSTimer;
import top.shadowpixel.shadowcore.util.plugin.DescriptionChecker;
import top.shadowpixel.shadowcore.util.plugin.ManagerUtils;
import net.dreampixel.dreamlevels.command.MainCommand;
import net.dreampixel.dreamlevels.config.ConfigManager;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import net.dreampixel.dreamlevels.hook.PlaceholderHook;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;

import java.io.File;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class DreamLevels extends AbstractPlugin {
    private static DreamLevels instance;

    private ConfigManager configManager;
    private LocaleManager localeManager;

    private DataManager dataManager;
    private SyncManager syncManager;

    private LevelManager levelManager;
    private RewardManager rewardManager;

    private boolean isEnabled = false;

    @Override
    public void enable() {
        var timer = new MSTimer();
        instance = this;

        // serializations
        registerSerializations(PlayerData.class, LevelData.class, Level.class);

        // config manager initialization
        this.configManager = new ConfigManager(this);
        this.configManager.initialize();
        logger.addReplacement("{prefix}", getPrefix());

        // locale manager initialization
        var localeFile = new File(getConfiguration().getString("locale.directory")
                .replace("{default}", getDataFolder().toString()));
        this.localeManager = new LocaleManager(this, localeFile);
        this.localeManager.initialize();
        if (getDefaultLocale() == LocaleManager.getInternal()) {
            this.logger.warn("The internal locale is in use!");
        }

        var lang = getDefaultMessage();
        this.logger.info(
                "",
                "",
                "&b&lDreamLevels &7>> &a" + lang.getString("startup.welcome") + "!",
                "",
                "&f" + lang.getString("startup.version") + ": &av" + getVersion(),
                "&f" + lang.getString("startup.author") + ": &aDreamStudio (MrTyphoon)",
                "",
                ""
        );

        /* Built-in description check */
        if (!new DescriptionChecker(
                this,
                "DreamLevels",
                "DreamStudio (MrTyphoon)",
                "1.0").check()) {
            MLogger.error("startup.on-enable.error-plugin_yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        /* Commands initialization */
        try {
            MLogger.info("startup.on-enable.register-command");
            initCommand();
            MLogger.info("startup.on-enable.succeed");
        } catch (Exception e) {
            MLogger.info("startup.on-enable.failed");
            Logger.error(e);
        }

        /* Listeners registration */
        try {
            MLogger.info("startup.on-enable.register-listener");
            registerListener("data", new DataListener());
            registerListener("level", new LevelListener());
            MLogger.info("startup.on-enable.succeed");
        } catch (Exception e) {
            MLogger.error("startup.on-enable.failed", e);
        }

        /* Listeners registration */
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                MLogger.info("startup.on-enable.register-PAPI");
                new PlaceholderHook().register();
                MLogger.info("startup.on-enable.succeed");
            } catch (Exception e) {
                MLogger.error("startup.on-enable.failed", e);
            }
        }

        // level manager
        this.levelManager = new LevelManager(this);
        this.levelManager.initialize();

        // reward manager
        this.rewardManager = new RewardManager(this);
        this.rewardManager.initialize();
        registerMenuHandler("reward");

        // sync manager
        initSyncService();

        // data manager
        this.dataManager = new DataManager(this);
        this.dataManager.initialize();

        Logger.info("");
        MLogger.infoReplaced("startup.on-enable.enabled",
                "{time}", String.valueOf(timer.getTimePassed()));
        isEnabled = true;

        // snapshot warning
//        Logger.warn("当前版本为测试版, 如有问题请联系开发者!");?
    }

    @Override
    public void disable() {
        if (!isEnabled) return;
        MLogger.infoReplaced("startup.on-disable.disabled");

        if (syncManager != null) {
            syncManager.unload();
        }

        ManagerUtils.unloadManagers(
                this.dataManager,
                this.localeManager,
                this.configManager,
                this.levelManager,
                this.rewardManager
        );
    }

    /**
     * @return Configuration
     */
    @NotNull
    public FiledConfiguration getConfiguration() {
        return requireNonNull(getConfiguration("Config"), "Configuration is null");
    }

    /**
     * @param name Name
     * @return Configuration
     */
    @Nullable
    public FiledConfiguration getConfiguration(String name) {
        return this.configManager.getFiledConfiguration(name);
    }

    /**
     * @return Default locale
     */
    public Locale getDefaultLocale() {
        return this.localeManager == null || this.localeManager.getDefaultLocale() == null ?
                LocaleManager.getInternal() : this.localeManager.getDefaultLocale();
    }

    /**
     * @return Message Configuration of default locale
     */
    public @NotNull Configuration getDefaultMessage() {
        return requireNonNull(getDefaultLocale().getConfig("Message"));
    }

    /**
     * @return Message Configuration of default events
     */
    public @NotNull Configuration getDefaultEvents() {
        return requireNonNull(getDefaultLocale().getConfig("Events"));
    }

    /**
     * Initialize commands.
     */
    public void initCommand() {
        getCommandHandler().addCommand(new MainCommand());

        // adapter for level
        getCommandHandler().createTypeAdapter(Level.class, value -> {
            var level = LevelManager.getInstance().getLevel(value.getString());
            if (level == null) {
                throw new ParameterizedCommandInterruptedException(value, "level not found");
            }

            return level;
        });

        // adapter for reward
        getCommandHandler().createTypeAdapter(RewardList.class, value -> {
            var reward = RewardManager.getInstance().getRewardList(value.getString());
            if (reward == null) {
                throw new ParameterizedCommandInterruptedException(value, "reward not found");
            }

            return reward;
        });
    }

    public void initSyncService() {
        if (syncManager != null) {
            syncManager.unload();
        }

        if (getConfiguration().getBoolean("data.sync-mode.enabled")) {
            // check ShadowMessenger
            if (Bukkit.getPluginManager().isPluginEnabled("ShadowMessenger")) {
                this.syncManager = new SyncManager(this);
                this.syncManager.initialize();
            } else {
                MLogger.error("data.sync-mode.ShadowMessenger-absent");
            }
        }
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @NotNull
    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    @NotNull
    public DataManager getDataManager() {
        return dataManager;
    }

    @Nullable
    public SyncManager getSyncManager() {
        return syncManager;
    }

    @NotNull
    public LevelManager getLevelManager() {
        return levelManager;
    }

    @NotNull
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public boolean isDebugMode() {
        return getConfiguration().getBoolean("Debug-mode", true);
    }

    @NotNull
    public static DreamLevels getInstance() {
        return instance;
    }

    /**
     * @return Prefix of this plugin
     */
    public static String getPrefix() {
        return getInstance().getConfiguration().getString("prefix");
    }

    /**
     * @return Version of this plugin
     */
    public static String getVersion() {
        return getInstance().getDescription().getVersion();
    }

    @NotNull @Deprecated //Shit
    public FileConfiguration getConfig() {
        return super.getConfig();
    }
}
