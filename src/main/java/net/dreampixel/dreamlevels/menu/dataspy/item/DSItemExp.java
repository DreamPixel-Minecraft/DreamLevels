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

public class DSItemExp extends MenuItem {
    private final LevelDataMenu menu;
    private final LevelData levelData;

    private ItemMeta originalMeta;

    public DSItemExp(LevelDataMenu menu) {
        super(Objects.requireNonNull(DataSpyManager.getInstance().getItemByKey("exp")));
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
            LocaleUtils.sendMessages(player, "data-spy.modify.exp",
                    "{player}", player.getName());
            DataInputController.getInstance().<Double>createInput()
                    .player(player)
                    .type(double.class)
                    .onInput(input -> handleInput(menu, input, player))
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

        // copy the original item meta, lest modifying all replacements
        // if the replacements are
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
        menu.setItem(1, 11, this);
    }

    @NotNull
    public Object getDisplayValue() {
        return levelData.getExp();
    }

    private void handleInput(LevelDataMenu menu, DataInputEntry<Double> input, Player player) {
        double previous = levelData.getExp();
        double value = input.getExistingValue();
        switch (input.getPrimitiveValue().charAt(0)) {
            case '+':
                levelData.addExp(value);
                break;
            case '-':
                levelData.removeExp(-value);
                break;
            default:
                levelData.setExp(value);
                break;
        }

        // send feedback command
        LocaleUtils.sendMessage(player, "data-spy.modified.exp",
                "{previous}", String.valueOf(previous),
                "{value}", String.valueOf(levelData.getExp()),
                "{player}", Objects.requireNonNull(levelData.getPlayer()).getName(),
                "{level}", levelData.getLevelName());

        // reopen the menu
        menu.openMenu(player);
    }
}
