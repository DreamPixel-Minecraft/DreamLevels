package net.dreampixel.dreamlevels.data.level;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class NullOfflineLevelData extends OfflineLevelData {
    private NullOfflineLevelData(@NotNull Consumer<String> feedbackConsumer) {
        this(null, feedbackConsumer, null);
    }

    private NullOfflineLevelData(UUID uniqueId, Consumer<String> feedbackConsumer, String levelName) {
        super(uniqueId, feedbackConsumer, levelName);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return UUID.randomUUID();
    }

    @Override
    public @NotNull String getLevelName() {
        return "";
    }

    @Override
    public void addLevels(int amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void setLevels(int amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void removeLevels(int amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void addExp(double amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void setExp(double amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void removeExp(double amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void setMultiple(double amount) {
        feedbackConsumer.accept("unknown level data");
    }

    @Override
    public void reset() {
        feedbackConsumer.accept("unknown level data");
    }

    public static NullOfflineLevelData of(@NotNull Consumer<String> feedbackConsumer) {
        return new NullOfflineLevelData(feedbackConsumer);
    }
}
