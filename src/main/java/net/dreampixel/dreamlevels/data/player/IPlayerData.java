package net.dreampixel.dreamlevels.data.player;

import net.dreampixel.dreamlevels.data.level.ILevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface IPlayerData {
    @NotNull UUID getUniqueId();

    @Nullable ILevelData getLevelData(@NotNull String level);

    @NotNull Map<String, ? extends ILevelData> getLevelData();

    void resetAll();
}
