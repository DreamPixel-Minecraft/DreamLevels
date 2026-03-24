package net.dreampixel.dreamlevels.task;

import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.level.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ExperienceBarTask extends BukkitRunnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(LevelManager.getInstance()::updateExperienceBar);
    }

    public void start() {
        runTaskTimer(DreamLevels.getInstance(), 200L, 200L);
    }

    public void stop() {
        try {
            cancel();
        } catch (Throwable ignored) {
        }
    }
}
