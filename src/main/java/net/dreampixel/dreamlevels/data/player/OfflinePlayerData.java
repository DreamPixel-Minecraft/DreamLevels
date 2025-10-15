package net.dreampixel.dreamlevels.data.player;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.OfflineLevelData;
import net.dreampixel.dreamlevels.util.SyncUtils;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.util.collection.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static net.dreampixel.dreamlevels.util.SyncUtils.*;

/**
 * A wrapper of offline player data's modifier. This wrapper offers modification operations for offline players' data.
 * <p>
 * If the server is on proxy mode and the sync-mode is enabled, the modifier will ask the proxy the player's server,
 * then ask the server to modify their data directly and send modification feedback. Then the modification result
 * will be accepted by the {@link #feedbackConsumer}
 * <p>
 * If the player cannot be found on all servers, the wrapper will load their offline data, then modify and save it.
 */
public class OfflinePlayerData implements IPlayerData {
    protected final UUID uniqueId;
    protected final Consumer<String> feedbackConsumer;
    protected final HashMap<String, OfflineLevelData> levelData = new HashMap<>(0);

    public OfflinePlayerData(@NotNull UUID uniqueId, @NotNull Consumer<String> feedbackConsumer) {
        this.uniqueId = uniqueId;
        this.feedbackConsumer = feedbackConsumer;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    @NotNull
    public OfflineLevelData getLevelData(@NotNull String level) {
        var data = MapUtils.smartMatch(level, levelData);
        if (data == null) {
            data = new OfflineLevelData(uniqueId, feedbackConsumer, level);
        }

        return data;
    }

    @Override
    public @NotNull Map<String, OfflineLevelData> getLevelData() {
        return this.levelData;
    }

    @Override
    public void resetAll() {
        // clear temp data
        levelData.clear();

        Runnable onOffline = () -> SyncUtils.handleOfflineData(uniqueId, feedbackConsumer, data -> {
            data.getLevelData().clear();
            feedbackConsumer.accept("ok offline");
        });

        if (!SyncUtils.isProxyMode()) {
            onOffline.run();
            return;
        }

        // clear all saved level data
        var message = getSyncChannel().queryBuilder()
                .receiver("*")
                .message("reset-all");
        sendRequest(
                uniqueId,
                message,
                feedbackConsumer,
                onOffline
        );
    }
}
