package net.dreampixel.dreamlevels.level;

import lombok.Getter;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.shadowpixel.shadowcore.api.config.component.NodeSection;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.util.text.StringUtils;

import java.util.HashMap;

/**
 * A container of a level's all events, for faster and more convenient invoking.
 */
@Getter
public class LevelEventContainer {
    private final String locale;
    private final String level;

    /**
     * Event objects for exp.
     */
    private final ExecutableEvent expReceivedEvent;
    private final ExecutableEvent expSetEvent;
    private final ExecutableEvent expRemovedEvent;

    /**
     * Event objects for levels.
     */
    private final ExecutableEvent levelsAddedEvent;
    // whether to use level up event instead of levels added events
    private boolean levelsAddedReplaced = false;
    private final ExecutableEvent levelsSetEvent;
    private final ExecutableEvent levelsRemovedEvent;

    private final ExecutableEvent multipleSetEvent;
    private final ExecutableEvent resetEventEvent;

    private final HashMap<String, ExecutableEvent> levelUpEvents = new HashMap<>();

    public LevelEventContainer(@NotNull String locale, @NotNull NodeSection section) {
        this.locale = locale;
        this.level = section.getPath();

        // load exp events
        this.expReceivedEvent = ExecutableEvent.of(section.getStringList("exp-received"));
        this.expSetEvent = ExecutableEvent.of(section.getStringList("exp-set"));
        this.expRemovedEvent = ExecutableEvent.of(section.getStringList("exp-removed"));

        // load level events
        this.levelsSetEvent = ExecutableEvent.of(section.getStringList("levels-set"));
        this.levelsRemovedEvent = ExecutableEvent.of(section.getStringList("levels-removed"));
        // ~ skip levels-added in this line
        // in order to check whether it should be replaced by level-up ones

        this.multipleSetEvent = ExecutableEvent.of(section.getStringList("multiple"));
        this.resetEventEvent = ExecutableEvent.of(section.getStringList("reset"));

        // load levels-added
        var levelsAdded = section.getStringList("levels-added");
        // if the first line is "LEVEL_UP",
        // then transfer the usage of levels-added to level-up
        if (!levelsAdded.isEmpty() && levelsAdded.get(0).equalsIgnoreCase("LEVEL_UP")) {
            levelsAddedReplaced = true;
            this.levelsAddedEvent = ExecutableEvent.emptyEvent();
        } else {
            this.levelsAddedEvent = ExecutableEvent.of(levelsAdded);
        }

        // load level-up
        var levelUp = section.getNodeSection("level-up-events");
        if (levelUp == null) {
            return;
        }

        for (var key : levelUp.getKeys()) {
            var event = ExecutableEvent.of(levelUp.getStringList(key));
            event.replacePermanently("{prefix}", DreamLevels.getPrefix());
            if (StringUtils.isInteger(key)) {
                event.replacePermanently("{levels}", key);
            }

            levelUpEvents.put(key.toUpperCase(), event);
        }
    }

    /**
     * Get the levels-added event.
     * <p>
     * If the levels-added event is replaced by the level-up event,
     * this method returns an empty event.
     * <p>
     * It is recommended to check the replacement status
     * via {@link #isLevelsAddedReplaced()} before invoking this method.
     *
     * @return Levels-added event
     */
    @NotNull
    public ExecutableEvent getLevelsAddedEvent() {
        return levelsAddedEvent;
    }

    /**
     * Get whether the levels-added event should be replaced by level-up events.
     * <p>
     * If the first line of levels-added event is set to 'LEVEL_UP', then it's
     * replaced. Once a player was executed 'AddLevels' command, the level-up event
     * is executed instead of levels-added event.
     *
     * @return Whether the levels-added event should be replaced by level-up events
     */
    public boolean isLevelsAddedReplaced() {
        return levelsAddedReplaced;
    }

    /**
     * Get the levels-added event. If the event is replaced by level-up ones,
     * then return event when upgrading to this level.
     * <p>
     * Please note that, return null if the level-up event is missing, however, a non-null
     * value if the levels-added event is unset (an empty ExecutableEvent object).
     *
     * @param level Level
     * @return Levels-added event, or level-up event at this level.
     */
    public @Nullable ExecutableEvent getAddLevelEvent(int level) {
        if (levelsAddedReplaced) {
            return getLevelUpEvent(level);
        }

        return levelsAddedEvent;
    }

    /**
     * Get the event that'll be executed when upgrading to a specific level.
     * Return {@code null} if there is no such event.
     *
     * @param level Specific level
     * @return Event
     */
    public @Nullable ExecutableEvent getLevelUpEvent(int level) {
        var event = levelUpEvents.get(String.valueOf(level));
        if (event == null) {
            event = levelUpEvents.get("DEFAULT");
        }

        return event;
    }
}
