package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerLevelsModifiedEvent extends AbstractLevelEvent {
    private final ModificationType type;
    private final int amount;

    public PlayerLevelsModifiedEvent(@NotNull Player who, Level level, ModificationType type, int amount) {
        super(who, level);
        this.type = type;
        this.amount = amount;
    }

    @NotNull
    public ModificationType getModificationType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
