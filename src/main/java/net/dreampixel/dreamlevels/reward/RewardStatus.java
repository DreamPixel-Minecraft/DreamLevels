package net.dreampixel.dreamlevels.reward;

import org.jetbrains.annotations.NotNull;

public enum RewardStatus {
    UNLOCKED ("reward-unlocked"),
    LOCKED ("reward-locked"),
    RECEIVED ("reward-received"),
    PERMISSIONS_DENIED ("reward-no-permissions");

    private final String itemKey;

    RewardStatus(String itemKey) {
        this.itemKey = itemKey;
    }

    @NotNull
    public String getItemKey() {
        return itemKey;
    }
}
