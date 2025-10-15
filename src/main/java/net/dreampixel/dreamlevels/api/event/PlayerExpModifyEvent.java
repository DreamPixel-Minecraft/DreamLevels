package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class PlayerExpModifyEvent extends AbstractLevelEvent implements Cancellable {
    private final ModificationType type;
    private final double amount;

    private boolean cancelled;

    /**
     * Fired when a player's exp changed. </br>
     *
     * Please pay attention that {@code amount} is the original value. </br>
     *
     * That's to say, you should multiply the amount by the multiple to get
     * the true value if the exp was added.
     *
     */
    public PlayerExpModifyEvent(@NotNull Player who, Level level, ModificationType type, double amount) {
        super(who, level);
        this.type = type;
        this.amount = amount;
    }

    @NotNull
    public ModificationType getModificationType() {
        return type;
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
