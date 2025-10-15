package net.dreampixel.dreamlevels.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class EventUtils {
    public static void fire(@NotNull Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static  <T extends Event & Cancellable> void fire(@NotNull T event,
                                                     @NotNull Event finished,
                                                     boolean fireEvent,
                                                     @NotNull Runnable runnable) {
        if (!fireEvent) {
            runnable.run();
            return;
        }

        fire(event);
        if (!event.isCancelled()) {
            runnable.run();
            fire(finished);
        }
    }
}
