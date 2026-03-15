package net.dreampixel.dreamlevels;

import lombok.Getter;
import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.listener.DataListener;
import net.dreampixel.dreamlevels.listener.LevelListener;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import net.dreampixel.dreamlevels.sync.SyncManager;
import net.dreampixel.dreamlevels.task.lifecycle.LifeCycleTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.command.exception.ParameterizedCommandInterruptedException;
import top.shadowpixel.shadowcore.api.config.component.Configuration;
import top.shadowpixel.shadowcore.api.config.component.FiledConfiguration;
import top.shadowpixel.shadowcore.api.locale.Locale;
import top.shadowpixel.shadowcore.api.menu.MenuHandler;
import top.shadowpixel.shadowcore.api.plugin.AbstractPlugin;
import top.shadowpixel.shadowcore.api.util.time.MSTimer;
import top.shadowpixel.shadowcore.util.plugin.ManagerUtils;
import net.dreampixel.dreamlevels.command.MainCommand;
import net.dreampixel.dreamlevels.config.ConfigManager;
import net.dreampixel.dreamlevels.locale.LocaleManager;
import net.dreampixel.dreamlevels.hook.PlaceholderHook;
import net.dreampixel.dreamlevels.util.Logger;
import net.dreampixel.dreamlevels.util.MLogger;

import java.io.File;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unchecked")
public final class DreamLevels extends AbstractPlugin {
    private static DreamLevels instance;

    private ConfigManager configManager;
    private LocaleManager localeManager;

    private DataManager dataManager;
    private SyncManager syncManager;

    private LevelManager levelManager;
    private RewardManager rewardManager;

    private DataSpyManager dataSpyManager;
    private LevelSpyManager levelSpyManager;

    /**
     * Menu handlers
     */
    @Getter
    private MenuHandler<DreamLevels> rewardMenuHandler;
    @Getter
    private MenuHandler<DreamLevels> dataSpyMenuHandler;
    @Getter
    private MenuHandler<DreamLevels> levelSpyMenuHandler;

    /**
     * Tasks
     */
    private LifeCycleTask lifeCycleTask;

    private boolean isEnabled = false;

    @Override
    public void enable() {
        var timer = new MSTimer();
        instance = this;

        // serializations
        registerSerializations(PlayerData.class, LevelData.class, Level.class);

        // config manager
        initConfigManager();

        // locale manager
        initLocaleManager();

        // show welcome messages
        this.logger.info(
                "&b",
                "&b    &l____                             __                _     ",
                "&b   &l/ __ \\________  ____ _____ ___  / /   ___ _   _____/ /____",
                "&b  &l/ / / / ___/ _ \\/ __ `/ __ `__ \\/ /   / _ \\ | / / _  / ___/",
                "&b &l/ /_/ / /  /  __/ /_/ / / / / / / /___/  __/ |/ /  __/ (__  ) ",
                "&b&l/_____/_/   \\___/\\__,_/_/ /_/ /_/_/____/\\___/|___/\\__,_/____/  ",
                "&3",
                "&7  > &fVersion: &b" + getDescription().getVersion(),
                "&7  > &fAuthor: &b" + getDescription().getAuthors()
        );

        /*
         * Basic functions of plugin (listeners, commands, etc.)
         */
        Logger.info("  &9[Basic Functions]");

        // init commands
        try {
            initCommand();
            Logger.info("&7  > &fCommand: &aSUCCESS");
        } catch (Exception e) {
            Logger.error("&7  > &fCommand: &cFAILED", e);
        }

        // init listeners
        try {
            registerListener("data", new DataListener());
            registerListener("level", new LevelListener());
            Logger.info("&7  > &fListener: &aSUCCESS");
        } catch (Exception e) {
            Logger.error("&7  > &fListener: &cFAILED", e);
        }

        // menu handlers
        this.rewardMenuHandler = (MenuHandler<DreamLevels>) getMenuHandler("reward");
        this.dataSpyMenuHandler = (MenuHandler<DreamLevels>) getMenuHandler("dataspy");
        this.levelSpyMenuHandler = (MenuHandler<DreamLevels>) getMenuHandler("levelspy");
        Logger.info("&7  > &fMenu Handlers: &aSUCCESS");

        /*
         * Featured functions.
         */
        Logger.info("  &9[Features]");

        // level manager
        initLevelManager();

        // reward manager
        initRewardManager();

        // sync manager
        initSyncService();

        // data manager
        this.dataManager = new DataManager(this);
        this.dataManager.initialize();

        // data spy manager
        this.dataSpyManager = new DataSpyManager(this);
        this.dataSpyManager.initialize();

        // level spy manager
        this.levelSpyManager = new LevelSpyManager(this);
        this.levelSpyManager.initialize();

        // start task
        startLifeCycleTask(false);

        /*
         * Hooks into other plugins.
         */
        Logger.info("  &9[Hooks]");

        // PlaceholderAPI
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                new PlaceholderHook().register();
                Logger.info("&7  > &fPlaceholderAPI: &aON");
            } catch (Exception e) {
                Logger.error("&7  > &fPlaceholderAPI: &cFAILED", e);
            }
        } else {
            Logger.info("&7  > &fPlaceholderAPI: &cOFF");
        }

        Logger.info("&9  [End]");
        Logger.info("&7  > &fStatus: &aREADY&7 &e(" + timer.getTimePassed() + "ms)");
        isEnabled = true;
    }

    @Override
    public void disable() {
        if (!isEnabled) return;
        MLogger.infoReplaced("startup.on-disable.disabled");

        stopLifeCycleTask();
        ManagerUtils.unloadManagers(
                this.syncManager,
                this.dataManager,
                this.localeManager,
                this.configManager,
                this.levelManager,
                this.rewardManager,
                this.dataSpyManager
        );
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
     * @return Configuration
     */
    @NotNull
    public FiledConfiguration getConfiguration() {
        return requireNonNull(getConfiguration("Config"), "Configuration is null");
    }

    /**
     * @return Items Configuration
     */
    @NotNull
    public FiledConfiguration getItemsConfiguration() {
        return requireNonNull(getConfiguration("Items"), "Items Configuration is null");
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

    /**
     * Initializethe sync-mode service.
     */
    public void initSyncService() {
        if (syncManager != null) {
            syncManager.unload();
        }

        if (getConfiguration().getBoolean("data.sync-mode.enabled")) {
            // check ShadowMessenger
            if (Bukkit.getPluginManager().isPluginEnabled("ShadowMessenger")) {
                this.syncManager = new SyncManager(this);
                this.syncManager.initialize();
                Logger.info("&7  > &fSync Mode: &aON");
            } else {
                Logger.error(
                        "&7  > &fSync Mode: &cFAILED",
                        "&7    &cThe plugin 'ShadowMessenger' is missing"
                );
            }

            return;
        }

        Logger.info("&7  > &fSync Mode: &cOFF");
    }

    /**
     * @param bypassCheck True to forcibly start the task whatever the configuration is enabled
     */
    public void startLifeCycleTask(boolean bypassCheck) {
        stopLifeCycleTask();

        // check whether enabled in the configuration
        if (!bypassCheck && !getConfiguration().getBoolean("menu-life-cycle.enabled")) {
            return;
        }

        // create task
        this.lifeCycleTask = new LifeCycleTask();
        lifeCycleTask.start();
    }

    public void stopLifeCycleTask() {
        if (lifeCycleTask != null) {
            lifeCycleTask.stop();
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

    @NotNull
    public DataSpyManager getDataSpyManager() {
        return dataSpyManager;
    }

    @NotNull
    public LevelSpyManager getLevelSpyManager() {
        return levelSpyManager;
    }

    public boolean isDebugMode() {
        return getConfiguration().getBoolean("Debug-mode", true);
    }

    /**
     * Initialize the config manager.
     */
    private void initConfigManager() {
        this.configManager = new ConfigManager(this);
        this.configManager.initialize();

        // put logger replacement
        logger.addReplacement("{prefix}", getPrefix());
    }

    /**
     * Initialize the locale manager.
     */
    private void initLocaleManager() {
        var localeFile = new File(getConfiguration().getString("locale.directory")
                .replace("{default}", getDataFolder().toString()));
        this.localeManager = new LocaleManager(this, localeFile);
        this.localeManager.initialize();

        // warn internal locale for invalid locale settings
        if (getDefaultLocale() == LocaleManager.getInternal()) {
            this.logger.warn("The internal locale is in use!");
        }
    }

    private void initLevelManager() {
        this.levelManager = new LevelManager(this);
        this.levelManager.initialize();

        // log loaded levels
        var levels = this.levelManager.getLevels();
        if (levels.isEmpty()) {
            Logger.info("&7  > &eNo levels were loaded. Please follow the instructions to create any.");
            return;
        }

        Logger.info("&7  > &fLevels Loaded: &e(" + levels.size() + " items)");
        levels.keySet().forEach(s -> Logger.info("&7    &e+ &f" + s));
    }

    private void initRewardManager() {
        this.rewardManager = new RewardManager(this);
        this.rewardManager.initialize();

        // log loaded rewards
        var rewardLists = rewardManager.getRewardLists();
        if (rewardLists.isEmpty()) {
            Logger.info("&7  > &eNo rewards were loaded. Please follow the instructions to create any.");
        } else {
            Logger.info("&7  > &fRewards loaded: &e(" + rewardLists.size() + " items)");
            rewardLists.keySet().forEach(s -> Logger.info("&7    &e+  &f" + s));
        }
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

    @SuppressWarnings("EmptyMethod")
    @NotNull @Deprecated //Shit
    public FileConfiguration getConfig() {
        Logger.warn("The default method \"public FileConfiguration getConfig()\" method has been invoked which is deprecated and may cause unexpected problems!");
        return super.getConfig();
    }
}
