package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerMultipleModifiedEvent extends AbstractLevelEvent {
    private final double amount;

    public PlayerMultipleModifiedEvent(@NotNull Player who, Level level, double amount) {
        super(who, level);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
