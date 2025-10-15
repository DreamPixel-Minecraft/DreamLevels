package net.dreampixel.dreamlevels.api;

import lombok.experimental.UtilityClass;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.data.player.OfflinePlayerData;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.uid.UUIDStorage;

import java.util.UUID;
import java.util.function.Consumer;

@UtilityClass
public class DreamLevelsAPI {

    /**
     * Get a level system.
     */
    @Nullable
    public static Level getLevel(@NotNull String name) {
        return LevelManager.getInstance().getLevel(name);
    }

    /**
     * Get a reward list.
     */
    @Nullable
    public static RewardList getRewardList(@NotNull String name) {
        return RewardManager.getInstance().getRewardList(name);
    }

    /**
     * Get a player data from a specific UUID.
     *
     * @param uniqueId UniqueId of player
     */
    @Nullable
    public static PlayerData getPlayerData(@NotNull UUID uniqueId) {
        return DataManager.getInstance().getPlayerData(uniqueId);
    }

    /**
     * Get the player's data.
     *
     * @param player Player
     * @return Player data
     */
    public static PlayerData getPlayerData(@NotNull Player player) {
        return DataManager.getInstance().getPlayerData(player);
    }
    /**
     * Get an offline player's data modifier. The modification result will be accepted
     * by the {@code feedbackConsumer}.
     *
     * @param uniqueId UniqueId of player
     * @param feedbackConsumer Feedback consumer
     * @return An offline player's data modifier.
     */
    public static OfflinePlayerData getOfflineDataModifier(@NotNull UUID uniqueId, @NotNull Consumer<String> feedbackConsumer) {
        return DataManager.getInstance().getOfflineDataModifier(uniqueId, feedbackConsumer);
    }

    /**
     * Get the plugin instance.
     */
    @NotNull
    public static DreamLevels getPlugin() {
        return DreamLevels.getInstance();
    }
}
