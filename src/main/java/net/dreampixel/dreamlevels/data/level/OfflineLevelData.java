package net.dreampixel.dreamlevels.data.level;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.api.event.ModificationType;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.util.SyncUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

import static net.dreampixel.dreamlevels.util.SyncUtils.*;

/**
 * A wrapper of offline player data's modifier. This wrapper offers modification operations for offline players' data.
 * <p>
 * If the server is on proxy mode and the sync-mode is enabled, the modifier will send modification request
 * to all servers to ensure the player is online, then ask their server to modify their data directly.
 * Then the modification result will be accepted by the {@link #feedbackConsumer}
 * <p>
 * If the player cannot be found on all servers, the wrapper will load their offline data, then modify and save it.
 */
public class OfflineLevelData implements ILevelData {
    protected final UUID uniqueId;
    protected final Consumer<String> feedbackConsumer;
    protected final String levelName;

    public OfflineLevelData(UUID uniqueId, Consumer<String> feedbackConsumer, String levelName) {
        this.uniqueId = uniqueId;
        this.feedbackConsumer = feedbackConsumer;
        this.levelName = levelName;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public @NotNull String getLevelName() {
        return this.levelName;
    }

    @Override
    public void addLevels(int amount) {
        modifyLevel(amount, ModificationType.ADD);
    }

    @Override
    public void setLevels(int amount) {
        modifyLevel(amount, ModificationType.SET);
    }

    @Override
    public void removeLevels(int amount) {
        modifyLevel(amount, ModificationType.REMOVE);
    }

    @Override
    public void addExp(double amount) {
        modifyExp(amount, ModificationType.ADD);
    }

    @Override
    public void setExp(double amount) {
        modifyExp(amount, ModificationType.SET);
    }

    @Override
    public void removeExp(double amount) {
        modifyExp(amount, ModificationType.REMOVE);
    }

    @Override
    public void setMultiple(double amount) {
        Runnable onOffline = () -> SyncUtils.handleOfflineLevelData(uniqueId, levelName, feedbackConsumer, levelData -> {
            levelData.setMultiple(amount, false);
            feedbackConsumer.accept("ok offline");
        });

        if (!SyncUtils.isProxyMode()) {
            onOffline.run();
            return;
        }

        var message = getSyncChannel().queryBuilder()
                .receiver("*")
                .message("modify-multiple")
                .withParameter("level", this.levelName)
                .withParameter("value", amount);
        sendRequest(
                uniqueId,
                message,
                feedbackConsumer,
                onOffline
        );
    }

    @Override
    public void reset() {
        Runnable onOffline = () -> SyncUtils.handleOfflineLevelData(uniqueId, levelName, feedbackConsumer, levelData -> {
            levelData.reset();
            feedbackConsumer.accept("ok offline");
        });

        if (!SyncUtils.isProxyMode()) {
            onOffline.run();
            return;
        }

        var message = getSyncChannel().queryBuilder()
                .receiver("*")
                .message("reset")
                .withParameter("level", this.levelName);
        sendRequest(
                uniqueId,
                message,
                feedbackConsumer,
                onOffline
        );
    }

    /**
     * @return Whether the sync mode is enabled, if true, the modification request will be post
     *         to where the player is if online, otherwise directly modify their offline data.
     */
    private boolean isProxyMode() {
        return DreamLevels.getInstance().getConfiguration()
                .getBoolean("data.sync-mode.enabled", false) && DreamLevels.getInstance().getSyncManager() != null;
    }

    private void modifyLevel(int amount, ModificationType type) {
        // handle the data offline
        Runnable onOffline = () -> SyncUtils.handleOfflineLevelData(uniqueId, levelName, feedbackConsumer, levelData -> {
            switch (type) {
                case ADD:
                    levelData.addLevels(amount, false);
                    break;
                case SET:
                    levelData.setLevels(amount, false);
                    break;
                case REMOVE:
                    levelData.removeLevels(amount, false);
                    break;
            }
            feedbackConsumer.accept("ok offline");
        });

        if (!SyncUtils.isProxyMode()) {
            onOffline.run();
            return;
        }

        var message = getSyncChannel().queryBuilder()
                .receiver("*")
                .message("modify-levels")
                .withParameter("level", this.levelName)
                .withParameter("player", this.uniqueId.toString())
                .withParameter("type", type.name())
                .withParameter("value", amount);
        sendRequest(
                uniqueId,
                message,
                feedbackConsumer,
                onOffline
        );
    }

    private void modifyExp(double amount, ModificationType type) {
        Runnable onOffline = () -> SyncUtils.handleOfflineLevelData(uniqueId, levelName, feedbackConsumer, levelData -> {
            switch (type) {
                case ADD:
                    levelData.addExp(amount, false);
                    break;
                case SET:
                    levelData.setExp(amount, false);
                    break;
                case REMOVE:
                    levelData.removeExp(amount, false);
                    break;
            }

            feedbackConsumer.accept("ok offline");
        });

        if (!SyncUtils.isProxyMode()) {
            onOffline.run();
            return;
        }

        var message = getSyncChannel().queryBuilder()
                .receiver("*")
                .message("modify-exp")
                .withParameter("level", this.levelName)
                .withParameter("type", type.name())
                .withParameter("value", amount);
        sendRequest(
                uniqueId,
                message,
                feedbackConsumer,
                onOffline
        );
    }
}
