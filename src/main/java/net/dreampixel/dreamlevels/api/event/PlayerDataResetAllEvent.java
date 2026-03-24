package net.dreampixel.dreamlevels.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerDataResetAllEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();

    public PlayerDataResetAllEvent(@NotNull Player who) {
        super(who);
    }

    @SuppressWarnings("unused")
    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
