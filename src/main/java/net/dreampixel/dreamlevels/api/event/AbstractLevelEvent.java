package net.dreampixel.dreamlevels.api.event;

import net.dreampixel.dreamlevels.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractLevelEvent extends PlayerEvent {
    protected static final HandlerList handlerList = new HandlerList();
    protected final Level level;

    public AbstractLevelEvent(@NotNull Player who, Level level) {
        super(who);
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
