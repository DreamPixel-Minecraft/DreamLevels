package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerDataResetEvent extends AbstractLevelEvent {
    public PlayerDataResetEvent(@NotNull Player who, Level level) {
        super(who, level);
    }
}
