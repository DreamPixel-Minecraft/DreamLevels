package net.dreampixel.dreamlevels.sync;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.util.MLogger;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.object.interfaces.Manager;
import top.shadowpixel.shadowmessenger.common.message.messenger.impl.bungee.BungeeChannel;
import top.shadowpixel.shadowmessenger.spigot.ShadowMessenger;

import static java.util.Objects.requireNonNull;

public class SyncManager implements Manager {
    private final DreamLevels plugin;

    private String channelName;
    private BungeeChannel channel;

    public SyncManager(DreamLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        MLogger.info("data.sync-mode.starting");
        var config = requireNonNull(plugin.getConfiguration().getNodeSection("data.sync-mode"), "sync-mode configuration is invalid");
        this.channelName = config.getString("channel");

        // check BungeeMessenger
        var messenger = ShadowMessenger.getBungeeMessenger();
        if (messenger == null) {
            MLogger.error("data.sync-mode.BungeeMessenger-absent");
            return;
        }

        // create channel and subscription
        var subscription = new SyncSubscription();
        this.channel = messenger.registerChannel(this.channelName);
        if (channel == null) {
            MLogger.error("data.sync-mode.channel-failure");
            return;
        }

        this.channel.addSubscription("main", subscription);
        MLogger.info("data.sync-mode.succeeded");
    }

    @Override
    public void unload() {
        if (channel != null) {
            channel.close();
        }

        MLogger.info("data.sync-mode.stopped");
    }

    @NotNull
    public BungeeChannel getChannel() {
        return channel;
    }

    @NotNull
    public String getChannelName() {
        return channelName;
    }

    @NotNull
    public static SyncManager getInstance() {
        return requireNonNull(DreamLevels.getInstance().getSyncManager(), "SyncManager still uninitialized");
    }
}
