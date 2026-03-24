package net.dreampixel.dreamlevels.menu.dataspy.item;

import lombok.var;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.input.DataInputController;
import top.shadowpixel.shadowcore.api.input.DataInputEntry;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;

public class DSItemMultiple extends MenuItem {
    private final LevelDataMenu menu;
    private final LevelData levelData;

    private ItemMeta originalMeta;

    public DSItemMultiple(LevelDataMenu menu) {
        super(Objects.requireNonNull(DataSpyManager.getInstance().getItemByKey("multiple")));
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
            LocaleUtils.sendMessages(player, "data-spy.modify.multiple",
                    "{player}", player.getName(),
                    "{level}", levelData.getLevelName());
            DataInputController.getInstance().<Double>createInput()
                    .player(player)
                    .type(double.class)
                    .onInput(input -> {
                        handleInput(menu, input, player);
                    })
                    .onInvalid(invalid -> LocaleUtils.sendMessage(player, "data-spy.invalid.number"))
                    .onCancelled(() -> {
                        LocaleUtils.sendMessage(player, "data-spy.cancel");
                        menu.openMenu(player);
                    })
                    .finish();
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
        menu.setItem(1, 12, this);
    }

    @NotNull
    public Object getDisplayValue() {
        return levelData.getMultiple();
    }

    private void handleInput(LevelDataMenu menu, DataInputEntry<Double> input, Player player) {
        double previous = levelData.getMultiple();
        levelData.setMultiple(input.getExistingValue());

        // send feedback command
        LocaleUtils.sendMessage(player, "data-spy.modified.multiple",
                "{previous}", String.valueOf(previous),
                "{value}", String.valueOf(levelData.getExp()),
                "{player}", Objects.requireNonNull(levelData.getPlayer()).getName(),
                "{level}", levelData.getLevelName());

        // reopen the menu
        menu.openMenu(player);
    }
}
