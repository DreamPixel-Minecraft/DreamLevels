package net.dreampixel.dreamlevels.reward;

import lombok.var;
import net.dreampixel.dreamlevels.DreamLevels;
import net.dreampixel.dreamlevels.util.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.function.component.ExecutableEvent;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.api.menu.impl.PlayerMenu;
import top.shadowpixel.shadowcore.api.util.item.ItemUtils;
import top.shadowpixel.shadowcore.util.entity.SenderUtils;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class RewardMenu extends PlayerMenu {
    private final RewardList rewardList;
    
    private final ArrayList<String> permissions;
    private boolean permissionsDenied = false;
    private ExecutableEvent permissionDeniedEvent;
    
    public RewardMenu(Player owner, RewardList rewardList) {
        super(owner, rewardList.getName(), rewardList.getName());
        this.rewardList = rewardList;

        // permissions
        var configuration = this.rewardList.getConfiguration();
        this.permissions = (ArrayList<String>) configuration.getStringList("reward-list.permissions");

        // skip constructing if the player has no permissions
        if (!SenderUtils.hasPermissions(owner, permissions)) {
            permissionsDenied = true;
            this.permissionDeniedEvent = ExecutableEvent.of(configuration.getStringList("reward-list.permissions-denied-events"));
            return;
        }

        constructMenu();
    }

    @Override
    public void openMenu() {
        if (!SenderUtils.hasPermissions(player, permissions)) {
            permissionDeniedEvent.execute(DreamLevels.getInstance(), player);
            return;
        }
        
        // now the player has enough permissions, loading the menu
        if (permissionsDenied) {
            constructMenu();
            permissionsDenied = false;
        }

        super.openMenu();
    }

    /**
     * Update items
     */
    public void updateItems() {
        var data = rewardList.getLevel().getLevelData(player);
        for (var reward : rewardList.getRewards()) {
            if (data.hasRewardReceived(rewardList, reward)) {
                continue;
            }

            Objects.requireNonNull(getPage(reward.getPage()))
                    .setItem(reward.getSlot(), getReplacedRewardItem(player, reward));
        }
    }

    public void constructMenu() {
        var configuration = rewardList.getConfiguration();

        // iteration for rewards
        var rewards = rewardList.getRewards();
        rewards.stream()
                .sorted(Comparator.comparingInt(Reward::getPage))
                .forEach(reward -> {
                    var page = getPage(reward.getPage());
                    if (page == null) {
                        // create new pages
                        var title = ReplaceUtils.coloredReplace(rewardList.getTitle(reward.getPage()), player,
                                "{page}", String.valueOf(reward.getPage()));
                        page = addPage(title, rewardList.getMenuSize());
                    }

                    // create item
                    var item = getReplacedRewardItem(player, reward);
                    page.setItem(reward.getSlot(), item);
                });

        // add other items
        var slotSection = configuration.getConfigurationSection("reward-list.item-slots");
        assert slotSection != null;
        for (var page : getPages().keySet()) {
            // custom items
            rewardList.getCustomItems().forEach((k, v) -> {
                // find slots
                var slotList = slotSection.getIntegerList(k);
                if (slotList.isEmpty()) {
                    Logger.warn("There are no slots set for custom item " + k + " in reward menu " + rewardList.getName());
                    return;
                }
                // put items
                slotList.forEach(slot -> setItem(page, slot, v));
            });

            // next-page item
            if (page != getPages().size()) {
                var item = rewardList.getItemByKey("next-page");
                item.addClickAction((menu, event) -> changePage(getCurrentPage() + 1));
                slotSection.isIntegerList("next-page",
                        list -> list.forEach(slot -> setItem(page, slot, item)));
            }

            // previous-page item
            if (page > 1) {
                var item = rewardList.getItemByKey("previous-page");
                item.addClickAction((menu, event) ->
                        changePage(getCurrentPage() - 1));
                slotSection.isIntegerList("previous-page",
                        list -> list.forEach(slot -> setItem(page, slot, item)));
            }
        }

        // set all slots unclickable
        for (var page : getPages().values()) {
            for (int i = 0; i < page.size(); i++) {
                page.getProperty().setUnclickable(i);
            }
        }
    }

    /**
     * Get an item from the reward for the player, and then replace its name and lore with necessary placeholders.
     *
     * @param player Player
     * @param reward Reward
     * @return Reward item
     */
    protected MenuItem getReplacedRewardItem(@NotNull Player player, @NotNull Reward reward) {
        var item = ItemUtils.replace(reward.getRewardItem(player), player,
                "{level}", String.valueOf(reward.getLevels()));
        // replace "{rewards}" with reward content in the lore
        var meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            var lore = meta.getLore();
            var idx = 0;
            //noinspection DataFlowIssue
            while (idx < lore.size()) {
                if (lore.get(idx).contains("{rewards}")) {
                    // remove the line and put all rewards
                    lore.remove(idx);
                    lore.addAll(idx, ReplaceUtils.coloredReplace(reward.getRewards(), player));
                    // move the pointer to the rewards' end
                    idx += reward.getRewards().size() - 1;
                }

                idx++;
            }

            // set item meta
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Create a reward menu for a player.
     *
     * @param player Player
     * @param rewardList Reward list
     * @return Reward menu
     */
    public static RewardMenu createMenu(@NotNull Player player, @NotNull RewardList rewardList) {
        var menu = new RewardMenu(player, rewardList);
        //noinspection DataFlowIssue
        DreamLevels.getInstance().getMenuHandler("reward")
                .addMenu(player.getUniqueId().toString(), menu);
        return menu;
    }
}
