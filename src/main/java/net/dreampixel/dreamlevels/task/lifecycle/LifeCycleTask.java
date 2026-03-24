package net.dreampixel.dreamlevels.task.lifecycle;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataOverallMenu;
import net.dreampixel.dreamlevels.menu.level.menu.LevelModificationMenu;
import net.dreampixel.dreamlevels.reward.RewardMenu;
import net.dreampixel.dreamlevels.util.Debugger;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public class LifeCycleTask extends BukkitRunnable {
    private static LifeCycleTask instance;

    private final LinkedHashMap<String, LifeCycled> lifeCycledItems = new LinkedHashMap<>();

    // life cycles
    private int rewardMenuCycle;
    private int levelDataMenuCycle;
    private int levelDataOverallMenuCycle;
    private int levelMenuCycle;

    public LifeCycleTask() {
        instance = this;
    }

    @NotNull
    public static LifeCycleTask getInstance() {
        return instance;
    }

    /**
     * Add an item to the task. If the task is not started, then it'll be ignored.
     *
     * @param item Item
     */
    public static void add(@NotNull LifeCycled item) {
        if (instance != null) {
            instance.addItem(item);
            Debugger.info("Item '" + item.getKey() + "' was added to the life cycle task.");
        }
    }

    /**
     * Remove an item from the task. If the task is not started, then it'll be ignored.
     *
     * @param key Key of the item
     */
    public static void remove(@NotNull String key) {
        if (instance != null) {
            instance.removeItem(key);
            Debugger.info("Item '" + key + "' was removed from the life cycle task.");
        }
    }

    /**
     * Start the task.
     */
    public void start() {
        // set cycle variables
        var config = DreamLevels.getInstance().getConfiguration()
                .getNodeSection("menu-life-cycle");
        if (config == null) {
            Logger.error("Invalid menu-life-cycle configuration. " +
                    "The menu life cycle task has shut down.");
            return;
        }

        // assign cycles
        rewardMenuCycle = config.getInt("reward-menu", 300);
        levelDataMenuCycle = config.getInt("level-data-menu", 60);
        levelDataOverallMenuCycle = config.getInt("level-data-overall-menu", 60);
        levelMenuCycle = config.getInt("level-menu", 60);

        // start the task async
        runTaskTimerAsynchronously(DreamLevels.getInstance(), 20, 20);
    }

    /**
     * Stop the task.
     */
    public void stop() {
        try {
            cancel();
        } catch (Throwable ignore) {
        }
    }

    /**
     * Add an life-cycled item to the task.
     *
     * @param item Item
     */
    public void addItem(@NotNull LifeCycled item) {
        if (item instanceof RewardMenu) {
            item.setLifeCycle(rewardMenuCycle);
        }

        if (item instanceof LevelDataMenu) {
            item.setLifeCycle(levelDataMenuCycle);
        }

        if (item instanceof LevelDataOverallMenu) {
            item.setLifeCycle(levelDataOverallMenuCycle);
        }

        if (item instanceof LevelModificationMenu) {
            item.setLifeCycle(levelMenuCycle);
        }

        lifeCycledItems.put(item.getKey(), item);
    }

    /**
     * Remove an item from the task, but do not destroy the item.
     *
     * @param key Key
     */
    public void removeItem(@NotNull String key) {
        lifeCycledItems.remove(key);
    }

    @Override
    public void run() {
        var iter = lifeCycledItems.entrySet().iterator();
        while (iter.hasNext()) {
            var item = iter.next();
            var value = item.getValue();
            var lifeCycle = value.getLifeCycle();
            value.setLifeCycle(lifeCycle - 1);

            // check the life cycle and remove
            if (value.getLifeCycle() < 0) {
                value.remove();
                iter.remove();
                Debugger.info("Item '" + value.getKey() + "''s life cycle finished, which has been removed.");
            }
        }
    }
}
