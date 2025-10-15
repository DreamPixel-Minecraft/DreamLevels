package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.sync.SyncManager;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowmessenger.common.message.messenger.impl.bungee.BungeeChannel;
import top.shadowpixel.shadowmessenger.common.message.messenger.impl.bungee.BungeeMessenger;
import top.shadowpixel.shadowmessenger.common.message.query.builder.QueryBuilder;

import java.util.UUID;
import java.util.function.Consumer;

@UtilityClass
public class SyncUtils {
    /**
     * Post a modification request to notify the player's current server to modify
     * their data. The feedback will be accepted by the Consumer {@code onFeedback}. </br>
     *
     * If the player is offline, then run the {@code onOffline} actions.
     *
     * @param request Request message
     * @param onOffline Actions to take if the player is offline
     */
    public static void sendRequest(@NotNull UUID uniqueId,
                             @NotNull QueryBuilder request,
                             @NotNull Consumer<String> feedbackConsumer,
                             @NotNull Runnable onOffline) {
        var channel = BungeeMessenger.getInstance().getDefaultChannel();
        if (channel == null) {
            feedbackConsumer.accept("invalid channel");
            return;
        }

        // get the player's server
        channel.queryBuilder()
                .receiver("proxy")
                .message("get-server")
                .deadline(30)
                .withParameter("player", uniqueId.toString())
                .whenTimeout(() -> feedbackConsumer.accept("timed out"))
                .whenComplete(r -> {
                    var feedback = r.message;
                    // run while the player's offline
                    if (feedback.equalsIgnoreCase("unknown player")) {
                        onOffline.run();
                        return;
                    }
                    // check "null" value
                    if (feedback.equalsIgnoreCase("null")) {
                        feedbackConsumer.accept("unknown server");
                        return;
                    }
                    // send query to the specific server
                    request
                            .receiver(feedback)
                            .withParameter("player", uniqueId.toString())
                            .whenTimeout(() -> feedbackConsumer.accept("timed out"))
                            .whenComplete(t -> feedbackConsumer.accept(t.message))
                            .buildAndQuery();
                })
                .buildAndQuery();
    }

    public static void handleOfflineData(@NotNull UUID uniqueId, Consumer<String> feedbackConsumer, Consumer<PlayerData> dataHandler) {
        var data = DataManager.getInstance().loadOfflineData(uniqueId);
        if (data == null) {
            feedbackConsumer.accept("unknown player");
            return;
        }

        dataHandler.accept(data);
        DataManager.getInstance().save(data);
    }

    public static void handleOfflineLevelData(@NotNull UUID uniqueId, @NotNull String level, Consumer<String> feedbackConsumer, Consumer<LevelData> dataHandler) {
        var data = DataManager.getInstance().loadOfflineData(uniqueId);
        if (data == null) {
            feedbackConsumer.accept("unknown player");
            return;
        }

        var levelData = data.getLevelData().get(level);
        if (levelData == null) {
            feedbackConsumer.accept("unknown level data");
            return;
        }

        dataHandler.accept(levelData);
        DataManager.getInstance().save(data);
    }

    /**
     * @return Whether the sync mode is enabled, if true, the modification request will be post
     *         to where the player is if online, otherwise directly modify their offline data.
     */
    public static boolean isProxyMode() {
        return DreamLevels.getInstance().getConfiguration()
                .getBoolean("data.sync-mode.enabled", false) && DreamLevels.getInstance().getSyncManager() != null;
    }

    @NotNull
    public static BungeeChannel getDefaultChannel() {
        return BungeeMessenger.getInstance().getDefaultChannel();
    }

    @NotNull
    public static BungeeChannel getSyncChannel() {
        return SyncManager.getInstance().getChannel();
    }
}
