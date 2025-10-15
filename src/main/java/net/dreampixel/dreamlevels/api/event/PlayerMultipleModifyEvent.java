package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class PlayerMultipleModifyEvent extends AbstractLevelEvent implements Cancellable {
    private final double amount;

    private boolean cancelled;

    public PlayerMultipleModifyEvent(@NotNull Player who, Level level, double amount) {
        super(who, level);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
