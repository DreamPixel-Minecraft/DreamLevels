package net.dreampixel.dreamlevels.hook;

import lombok.var;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.dreampixel.dreamlevels.level.LevelManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.util.collection.ArrayUtils;
import net.dreampixel.dreamlevels.DreamLevels;
import top.shadowpixel.shadowcore.util.collection.ListUtils;
import top.shadowpixel.shadowcore.util.object.NumberUtils;

import java.util.List;

/**
 * A hook into PlaceholderAPI, providing placeholder service.
 * The main placeholder is %dreamlevels_<arg1>_<arg2>%
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class PlaceholderHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "DreamLevels";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DreamStudio (MrTyphoon)";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        var parameters = params.split("_");
        if (ArrayUtils.isNull(parameters)) {
            return null;
        }

        var level = LevelManager.getInstance().getLevel(parameters[1]);
        if (level == null) {
            return null;
        }

        var levelData = level.getLevelData(player);
        switch (parameters[0].toLowerCase()) {
            // %dreamlevels_levels_<level>%
            case "levels":
                return String.valueOf(levelData.getLevels());
            // %dreamlevels_exp_<level>%
            case "exp":
                return String.valueOf(levelData.getExp());
            case "required-exp":
                return String.valueOf(levelData.getRequiredExp());
            case "max-levels":
                return String.valueOf(level.getMaxLevels(player));
            case "multiple":
                return String.valueOf(levelData.getMultiple());
            case "ismax":
                return String.valueOf(levelData.isMax());
            // %dreamlevels_progressbar_<level>_<length>%
            case "percentage":
                return NumberUtils.cutString(levelData.getPercentage(), 2);
            case "display-name":
                return level.getDisplayName();
            case "color":
                return levelData.getColor();
            case "progressbar":
                if (parameters.length > 3) {
                    var length = NumberUtils.parseInt(parameters[2], -1);
                    if (length < 1) {
                        return "invalid length";
                    }

                    return levelData.getProgressBar(length);
                }

                return levelData.getProgressBar();
            // %dreamlevels_rewards-count_<level>_<reward>%
            case "received-rewards-count":
                var rewardList = levelData.getReceivedRewards(parameters[2]);
                if (rewardList == null) {
                    return "no such reward";
                }

                return String.valueOf(rewardList.size());
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return ListUtils.asList(
                "levels",
                "exp",
                "progressbar"
        );
    }
}
