package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class PlayerLevelsModifyEvent extends AbstractLevelEvent implements Cancellable {
    private final ModificationType type;
    private final int amount;

    private boolean cancelled;

    public PlayerLevelsModifyEvent(@NotNull Player who, Level level, ModificationType type, int amount) {
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
