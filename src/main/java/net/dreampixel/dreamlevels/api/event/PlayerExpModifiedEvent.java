package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerExpModifiedEvent extends AbstractLevelEvent {
    private final ModificationType type;
    private final double amount;

    public PlayerExpModifiedEvent(@NotNull Player who, Level level, ModificationType type, double amount) {
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
}
