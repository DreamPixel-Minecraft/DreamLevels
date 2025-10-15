package net.dreampixel.dreamlevels.data.level;

import lombok.Getter;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.api.event.*;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.level.LevelManager;
import net.dreampixel.dreamlevels.reward.Reward;
import net.dreampixel.dreamlevels.reward.RewardList;
import net.dreampixel.dreamlevels.reward.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.util.collection.MapUtils;
import top.shadowpixel.shadowcore.util.object.NumberUtils;
import top.shadowpixel.shadowcore.util.text.ColorUtils;

import java.util.*;

import static net.dreampixel.dreamlevels.util.EventUtils.*;
import static top.shadowpixel.shadowcore.util.object.NumberUtils.*;

@SerializableAs("DreamLevels-LevelData")
@Getter
public class LevelData implements ConfigurationSerializable, ILevelData {
    private UUID uniqueId;
    private String level;

    // data
    private int levels = 0;
    private double exp = 0;
    private double multiple = 1.0d;
    private Map<String, List<String>> receivedRewards = new HashMap<>();

    public LevelData(@NotNull UUID uniqueId, @NotNull String level) {
        this.uniqueId = uniqueId;
        this.level = level;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public LevelData(@NotNull Map<String, Object> map) {
        this.levels = asInt(map.get("levels"), 0);
        this.exp = asDouble(map.get("exp"), 0);
        this.multiple = asDouble(map.get("multiple"), 0.0d);
        this.receivedRewards = new HashMap<>((Map<String, List<String>>) map.get("received-rewards"));
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the level of this level data. </br>
     *
     * Return null if the server doesn't have the level, usually for offline usage.
     * Use {@link #getLevelName()} for only level name.
     *
     * @return Level
     */
    @Nullable
    public Level getLevel() {
        return LevelManager.getInstance().getLevel(this.level);
    }

    /**
     * @return Level name
     */
    @Override
    public @NotNull String getLevelName() {
        return this.level;
    }

    /**
     * Get the owner of this level data. Returns null if the player is offline.
     */
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    /**
     * Add levels. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void addLevels(int amount) {
        addLevels(amount, true);
    }

    /**
     * Add levels. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void addLevels(int amount, boolean fireEvent) {
        modifyLevel(amount, ModificationType.ADD, fireEvent);
    }

    /**
     * Set levels. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void setLevels(int amount) {
        setLevels(amount, true);
    }

    /**
     * Set levels. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void setLevels(int amount, boolean fireEvent) {
        modifyLevel(amount, ModificationType.SET, fireEvent);
    }

    /**
     * Remove levels. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void removeLevels(int amount) {
        removeLevels(amount, true);
    }

    /**
     * Remove levels. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void removeLevels(int amount, boolean fireEvent) {
        modifyLevel(amount, ModificationType.REMOVE, fireEvent);
    }

    /**
     * Add experience. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void addExp(double amount) {
        addExp(amount, true);
    }

    /**
     * Add experience. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void addExp(double amount, boolean fireEvent) {
        modifyExp(amount, ModificationType.ADD, fireEvent);
    }

    /**
     * Set experience. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void setExp(double amount) {
        setExp(amount, true);
    }

    /**
     * Set experience. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void setExp(double amount, boolean fireEvent) {
        modifyExp(amount, ModificationType.SET, fireEvent);
    }

    /**
     * Remove experience. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void removeExp(double amount) {
        removeExp(amount, true);
    }

    /**
     * Remove experience. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void removeExp(double amount, boolean fireEvent) {
        modifyExp(amount, ModificationType.REMOVE, fireEvent);
    }

    /**
     * Set multiple. This operation will be cancelled if the event is fired and then cancelled.
     */
    @Override
    public void setMultiple(double amount) {
        setMultiple(amount, true);
    }

    /**
     * Set multiple. This operation will be cancelled if the event is fired and then cancelled.
     *
     * @param fireEvent Whether to call the event.
     *                  If false, the data will be modified without event being fired
     */
    public void setMultiple(double amount, boolean fireEvent) {
        // check illegal amount
        if (amount <= 0) {
            return;
        }

        // skip when player or level is null
        if (getPlayer() == null || getLevel() == null && !fireEvent) {
            return;
        }

        fire(
                new PlayerMultipleModifyEvent(getPlayer(), getLevel(), amount),
                new PlayerMultipleModifiedEvent(getPlayer(), getLevel(), amount),
                fireEvent,
                () -> this.multiple = amount
        );
    }

    /**
     * Clear the player's data.
     */
    @Override
    public void reset() {
        this.levels = 0;
        this.exp = 0.0d;
        this.multiple = 1.0d;
        this.receivedRewards.clear();

        // reset all menus
        var player = getPlayer();
        if (player != null) {
            RewardManager.getInstance().removeRewardMenu(player);
        }

        // fire event
        var level = getLevel();
        if (level != null && player != null) {
            fire(new PlayerDataResetEvent(player, level));
        }
    }

    /**
     * @param reward Reward name
     * @return All received rewards
     */
    @Nullable
    public List<String> getReceivedRewards(@NotNull String reward) {
        return this.receivedRewards.get(reward);
    }

    /**
     * @return Exp required leveling up.
     */
    public double getRequiredExp() {
        var level = getLevel();
        if (level == null) {
            return Double.MAX_VALUE;
        }

        return level.getRequiredExp(this.levels + 1);
    }

    /**
     * @return Percentage
     */
    public double getPercentage() {
        var percentage = exp / getRequiredExp() * 100F;
        return Double.isNaN(percentage) ? 100.0d : Math.abs(Math.min(percentage, 100.0d));
    }

    /**
     * @return Progress bar
     */
    public String getProgressBar() {
        return getProgressBar(DreamLevels.getInstance().getConfiguration().getInt("level.progress-bar.default-length"));
    }

    /**
     * @param length Length of progress bar
     * @return Progress bar
     */
    public String getProgressBar(int length) {
        if (length <= 0) {
            return "";
        }

        var config = DreamLevels.getInstance().getConfiguration().getConfigurationSection("level.progress-bar.");
        assert config != null;

        var filled = ColorUtils.colorize(config.getString("filled", "&b■"));
        var blank = ColorUtils.colorize(config.getString("blank", "&7■"));

        // build progress bar string
        var progress = new StringBuilder();
        var filled_length = (int) (getPercentage() / 100F * length);
        for (int i = 0; i < length; i++) {
            if (i < filled_length) {
                // avoid appending duplicated color strings ("&b■&b■&b■" -> "&b■■■")
                if (ChatColor.getLastColors(filled).equals(ChatColor.getLastColors(progress.toString()))) {
                    progress.append(ChatColor.stripColor(filled));
                    continue;
                }

                progress.append(filled);
            } else {
                // avoid appending duplicated color strings ("&7■&7■&7■" -> "&7■■■")
                if (ChatColor.getLastColors(blank).equals(ChatColor.getLastColors(progress.toString()))) {
                    progress.append(ChatColor.stripColor(blank));
                    continue;
                }

                progress.append(blank);
            }
        }

        return progress.toString();
    }

    @NotNull
    public String getColor() {
        if (getLevel() == null) {
            return "";
        }

        return getLevel().getColor(this.levels);
    }

    public boolean isMax() {
        if (getLevel() == null) {
            return false;
        }

        return getLevel().isMax(this.levels);
    }

    /**
     * @return Total experience that the player has ever received
     */
    public double getTotalExp() {
        var level = getLevel();
        if (level == null) {
            return -1;
        }

        var total = 0;
        for (int i = 1; i <= levels; i++) {
            var exp = level.getRequiredExp(i);
            if (exp < 0) {
                break;
            }

            total += exp;
        }

        return total;
    }

    /**
     * Check whether the player has received this reward.
     */
    public boolean hasRewardReceived(@NotNull String reward, @NotNull String name) {
        if (!receivedRewards.containsKey(reward)) {
            return false;
        }

        var rewards = receivedRewards.get(reward);
        return rewards.contains(name);
    }

    /**
     * Check whether the player has received this reward.
     */
    public boolean hasRewardReceived(RewardList rewardList, Reward reward) {
        return hasRewardReceived(rewardList.getName(), reward.getName());
    }

    public void addReceivedReward(@NotNull String rewardList, @NotNull String reward) {
        var list = getReceivedRewards(rewardList);
        if (list == null) {
            list = new ArrayList<>();
            receivedRewards.put(rewardList, list);
        }

        list.add(reward);
    }

    /**
     * Check level up.
     */
    public void checkLevelUp() {
        var level = getLevel();
        var player = getPlayer();
        if (level == null || player == null || isMax()) {
            return;
        }

        if (this.exp >= getRequiredExp()) {
            while (this.exp >= getRequiredExp() && this.levels < getMaxLevels()) {
                this.exp -= getRequiredExp();
                this.levels++;

                // execute events
                var event = level.getLevelUpEvent(player, this.levels);
                if (event != null) {
                    event.replace("{previous}", String.valueOf(levels - 1));
                    event.replace("{levels}", String.valueOf(levels));
                    event.execute(DreamLevels.getInstance(), player);
                }
            }

            fire(new PlayerLevelUpEvent(player, level));
            // refresh reward menu items
            RewardManager.getInstance().updateRewardMenus(player, level);
        }
    }

    /**
     * Get the max levels. Returns -1 if the level or the player is null.
     */
    public int getMaxLevels() {
        if (getLevel() == null || getPlayer() == null) {
            return -1;
        }

        return getLevel().getMaxLevels(getPlayer());
    }

    public double getMultiple() {
        var player = getPlayer();
        if (player == null) {
            return this.multiple;
        }

        // find effective permission that decided the multiple
        var node = "dreamlevels.levels." + this.level + ".multiple.";
        for (var permission : player.getEffectivePermissions()) {
            if (permission.getPermission().toLowerCase().startsWith(node.toLowerCase())) {
                return NumberUtils.asDouble(permission.getPermission().substring(node.length()), multiple);
            }
        }

        return multiple;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return MapUtils.of(
                "levels", levels,
                "exp", exp,
                "multiple", multiple,
                "received-rewards", receivedRewards
        );
    }

    private void modifyExp(double amount, ModificationType type, boolean fireEvent) {
        // check illegal amount
        if (amount <= 0) {
            return;
        }

        // skip when player or level is null
        if ((getPlayer() == null || getLevel() == null) && fireEvent) {
            return;
        }

        fire(
                new PlayerExpModifyEvent(getPlayer(), getLevel(), type, amount),
                new PlayerExpModifiedEvent(getPlayer(), getLevel(), type, amount),
                fireEvent,
                () -> {
                    switch (type) {
                        case ADD:
                            this.exp += amount * getMultiple();
                            break;
                        case SET:
                            this.exp = amount;
                            break;
                        case REMOVE:
                            this.exp -= amount;
                            break;
                    }
                }
        );
    }

    public void setLevel(@NotNull String level) {
        if (this.level == null) {
            this.level = level;
        }
    }

    public void setUniqueId(@NotNull UUID uniqueId) {
        if (this.uniqueId == null) {
            this.uniqueId = uniqueId;
        }
    }

    private void modifyLevel(int amount, ModificationType type, boolean fireEvent) {
        // check illegal amount
        if (amount <= 0) {
            return;
        }

        // skip when player or level is null
        if ((getPlayer() == null || getLevel() == null) && fireEvent) {
            return;
        }

        fire(
                new PlayerLevelsModifyEvent(getPlayer(), getLevel(), type, amount),
                new PlayerLevelsModifiedEvent(getPlayer(), getLevel(), type, amount),
                fireEvent,
                () -> {
                    switch (type) {
                        case ADD:
                            this.levels += amount;
                            break;
                        case SET:
                            this.levels = amount;
                            break;
                        case REMOVE:
                            this.levels -= amount;
                            break;
                    }
                }
        );
    }
}
