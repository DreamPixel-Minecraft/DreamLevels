package net.dreampixel.dreamlevels.reward;

import lombok.Getter;
import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.config.component.NodeSection;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.api.menu.impl.PlayerMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemBuilder;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Getter
public class Reward {
    private final RewardList parent;
    private final String name;
    private final NodeSection configuration;

    private final int levels;
    private final List<String> rewards;
    private final List<String> permissions;
    private final ExecutableEvent event;

    // for menu
    private final int page;
    private final int slot;

    // custom
    private final HashMap<String, MenuItem> customItems = new HashMap<>();

    public Reward(@NotNull RewardList parent, @NotNull NodeSection configuration) {
        this.parent = parent;
        this.name = configuration.getPath();
        this.configuration = configuration;

        this.levels = configuration.getInt("levels");
        this.rewards = configuration.getStringList("rewards");
        this.permissions = configuration.getStringList("permissions");
        this.event = ExecutableEvent.of(configuration.getStringList("events"));

        this.page = configuration.getInt("page");
        this.slot = configuration.getInt("slot");

        loadCustomItems();
    }

    public void loadCustomItems() {
        configuration.isNodeSection("custom-items", s -> {
            for (var key : s.getKeys()) {
                var custom = s.getNodeSection(key);
                if (custom == null) {
                    return;
                }

                var item = ItemBuilder.builder(custom).build();
                var menuItem = MenuItem.of(item);

                // add click events
                var events = custom.getStringList("events");
                if (!events.isEmpty()) {
                    var ee = ExecutableEvent.of(events);
                    menuItem.addClickAction((menu, event) -> {
                        ee.execute(DreamLevels.getInstance(), (Player) event.getWhoClicked());
                        event.setCancelled(true);
                    });
                }

                this.customItems.put(key, menuItem);
            }
        });
    }

    /**
     * @param key Key of item
     * @return A copy of reward item
     */
    @NotNull
    public MenuItem getItemByKey(@NotNull String key) {
        // get from the reward
        var item = this.customItems.get(key);
        // get from the reward list
        if (item == null) {
            item = this.parent.getCustomItems().get(key);
        }
        // get from the default item configuration
        if (item == null) {
            item = RewardManager.getInstance().defaultItems.get(key);
        }
        // clone
        item = Objects.requireNonNull(item, "unknown reward item " + key).clone();
        // add unlock events
        if (key.equalsIgnoreCase("reward-unlocked")) {
            item.addClickAction(((menu, event1) -> {
                event.execute(DreamLevels.getInstance(), (Player) event1.getWhoClicked());
                // add received rewards
                var player = (Player) event1.getWhoClicked();
                var data = parent.getLevel().getLevelData(player);
                data.addReceivedReward(parent.getName(), this.name);

                // update the unlocked item to received one
                var pm = ((RewardMenu) menu);
                pm.setItem(event1.getSlot(), ((RewardMenu) menu).getReplacedRewardItem(player, this));
            }));
        }

        return item;
    }

    @NotNull
    public MenuItem getRewardItem(@NotNull RewardStatus status) {
        return getItemByKey(status.getItemKey());
    }

    public MenuItem getRewardItem(@NotNull Player player) {
        RewardStatus status = getRewardStatus(player);
        var item = getRewardItem(status);

        // add default events
        if (item.getClickActions().isEmpty()) {
            var events = LocaleUtils.getLocaleEvents(player);
            var ee = (ExecutableEvent) null;
            switch (status) {
                case LOCKED:
                    ee = ExecutableEvent.of(events.getStringList("rewards.locked"));
                    break;
                case RECEIVED:
                    ee = ExecutableEvent.of(events.getStringList("rewards.received"));
                    break;
                case PERMISSIONS_DENIED:
                    ee = ExecutableEvent.of(events.getStringList("rewards.no-permission"));
                    break;
            }

            if (ee != null) {
                var ee_ = ee;
                ee_.replacePermanently("{prefix}", DreamLevels.getPrefix());
                ee_.replace("{player}", player.getName());
                item.addClickAction((menu, event) -> ee_.execute(DreamLevels.getInstance(), ((Player) event.getWhoClicked())));
            }
        }

        return ItemUtils.replace(item, player);
    }

    @NotNull
    public RewardStatus getRewardStatus(@NotNull Player player) {
        var data = parent.getLevel().getLevelData(player);

        if (data.hasRewardReceived(parent.getName(), this.name)) {
            return RewardStatus.RECEIVED;
        }

        if (!SenderUtils.hasPermissions(player, permissions)) {
            return RewardStatus.PERMISSIONS_DENIED;
        }

        if (data.getLevels() < levels) {
            return RewardStatus.LOCKED;
        }

        return RewardStatus.UNLOCKED;
    }
}
