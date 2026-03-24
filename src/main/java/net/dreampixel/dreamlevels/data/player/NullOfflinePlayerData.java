package net.dreampixel.dreamlevels.data.player;

import net.dreampixel.dreamlevels.data.level.NullOfflineLevelData;
import net.dreampixel.dreamlevels.data.level.OfflineLevelData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("DataFlowIssue")
public class NullOfflinePlayerData extends OfflinePlayerData {
    public NullOfflinePlayerData(@NotNull Consumer<String> feedbackConsumer) {
        super(null, feedbackConsumer);
    }

    public static @NotNull OfflinePlayerData of(@NotNull Consumer<String> feedbackConsumer) {
        return new NullOfflinePlayerData(feedbackConsumer);
    }

    @Override
    public void resetAll() {
        feedbackConsumer.accept("unknown player");
    }

    @Override
    public @NotNull Map<String, OfflineLevelData> getLevelData() {
        return Collections.emptyMap();
    }

    @Override
    public @NotNull OfflineLevelData getLevelData(@NotNull String level) {
        return NullOfflineLevelData.of(super.feedbackConsumer);
    }
}
