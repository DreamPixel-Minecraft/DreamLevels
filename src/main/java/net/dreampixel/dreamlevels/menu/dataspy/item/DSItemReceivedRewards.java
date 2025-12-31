package net.dreampixel.dreamlevels.menu.dataspy.item;

import lombok.var;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.input.DataInputController;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;

public class DSItemReceivedRewards extends MenuItem {
    private final LevelDataMenu menu;
    private final LevelData levelData;

    private ItemMeta originalMeta;

    public DSItemReceivedRewards(LevelDataMenu menu) {
        super(Objects.requireNonNull(DataSpyManager.getInstance().getItemByKey("receive-rewards")));
        this.menu = menu;
        this.levelData = menu.getLevel().getLevelData(menu.getUniqueId());

        if (levelData == null) {
            return;
        }

        // menu item settings
        updateItem();

        addClickAction(event -> {
            var player = event.getPlayer();
            player.closeInventory();
            LocaleUtils.sendMessages(player, "data-spy.modify.receive-rewards");
            DataInputController.getInstance().createInput(player, double.class,
                    input -> {
                        levelData.setMultiple(input.getExistingValue());
                        menu.openMenu(player);
                    },
                    invalid -> LocaleUtils.sendMessage(player, "data-spy.invalid.number"),
                    () -> {
                        LocaleUtils.sendMessage(player, "data-spy.cancel");
                        menu.openMenu(player);
                    });
        });
    }

    public void updateItem() {
        var meta = getItemMeta();
        if (meta == null) {
            return;
        }

        if (originalMeta == null) {
            originalMeta = meta.clone();
        }

        var player = levelData.getPlayer();
        if (player == null) {
            return;
        }

        // replace display name
        meta.setDisplayName(ReplaceUtils.coloredReplace(originalMeta.getDisplayName(), player,
                "{value}", getDisplayValue().toString(),
                "{player}", player.getName(),
                "{level}", levelData.getLevelName()));

        // replace lore
        if (meta.hasLore()) {
            //noinspection DataFlowIssue
            meta.setLore(ReplaceUtils.coloredReplace(originalMeta.getLore(), player,
                    "{value}", getDisplayValue().toString(),
                    "{player}", player.getName(),
                    "{level}", levelData.getLevelName()));
        }

        setItemMeta(meta);
        menu.setItem(1, 10, this);
    }

    @NotNull
    public Object getDisplayValue() {
        return levelData.getMultiple();
    }
}
