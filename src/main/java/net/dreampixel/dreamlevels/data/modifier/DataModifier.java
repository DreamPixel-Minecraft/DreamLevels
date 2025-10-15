package net.dreampixel.dreamlevels.data.modifier;

import net.dreampixel.dreamlevels.data.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DataModifier {

    @Nullable
    PlayerData load(@NotNull UUID uuid);

    boolean create(@NotNull UUID uuid, @NotNull PlayerData data);

    boolean remove(@NotNull UUID uuid);

    boolean save(@NotNull PlayerData data);

    boolean exist(@NotNull UUID uuid);

    @NotNull
    StorageMethod[] getStorageMethod();

    default boolean create(@NotNull UUID uuid) {
        return create(uuid, new PlayerData(uuid));
    }
}
