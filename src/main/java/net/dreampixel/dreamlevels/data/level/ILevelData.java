package net.dreampixel.dreamlevels.data.level;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ILevelData {
    @NotNull UUID getUniqueId();

    @NotNull String getLevelName();

    void addLevels(int amount);

    void setLevels(int amount);

    void removeLevels(int amount);

    void addExp(double amount);

    void setExp(double amount);

    void removeExp(double amount);

    void setMultiple(double amount);

    void reset();
}
