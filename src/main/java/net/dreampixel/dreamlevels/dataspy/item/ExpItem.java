package net.dreampixel.dreamlevels.dataspy.item;

import lombok.var;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.input.DataInputController;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;

public class ExpItem extends MenuItem {
    private final LevelDataMenu menu;
    private final LevelData levelData;

    private ItemMeta originalMeta;

    public ExpItem(LevelDataMenu menu) {
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
            DataInputController.getInstance().createInput(player, double.class,
                    input -> {
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
                                "{player}", Objects.requireNonNull(levelData.getPlayer()).getName());

                        // reopen the menu
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
        meta.setDisplayName(ReplaceUtils.replace(originalMeta.getDisplayName(),
                "{value}", getDisplayValue().toString(),
                "{player}", player.getName()));

        // replace lore
        if (meta.hasLore()) {
            //noinspection DataFlowIssue
            meta.setLore(ReplaceUtils.replace(originalMeta.getLore(),
                    "{value}", getDisplayValue().toString(),
                    "{player}", player.getName()));
        }

        setItemMeta(meta);
        menu.setItem(1, 11, this);
    }

    @NotNull
    public Object getDisplayValue() {
        return levelData.getExp();
    }
}
