package net.dreampixel.dreamlevels.data.modifier;

import net.dreampixel.dreamlevels.data.player.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;

public interface FileDataModifier extends DataModifier {

    @Nullable
    PlayerData load(@NotNull UUID uuid, @NotNull File file);

    PlayerData load(@NotNull UUID uuid, @NotNull Reader reader);

    @NotNull
    File getDataFile(@NotNull UUID uuid);

    @Override
    default boolean exist(@NotNull UUID uuid) {
        return getDataFile(uuid).exists();
    }

    @Nullable
    default PlayerData load(@NotNull UUID uuid, @NotNull InputStream is) {
        return load(uuid, new InputStreamReader(is));
    }
}
