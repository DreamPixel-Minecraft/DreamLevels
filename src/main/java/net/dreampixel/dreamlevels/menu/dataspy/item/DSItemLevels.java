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

public class DSItemLevels extends MenuItem {
    private final LevelDataMenu menu;
    private final LevelData levelData;

    private ItemMeta originalMeta;

    public DSItemLevels(LevelDataMenu menu) {
        super(Objects.requireNonNull(DataSpyManager.getInstance().getItemByKey("levels")));
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
            LocaleUtils.sendMessages(player, "data-spy.modify.levels",
                    "{player}", player.getName(),
                    "{level}", levelData.getLevelName());
            DataInputController.getInstance().createInput(player, int.class,
                    input -> {
                        int previous = levelData.getLevels();
                        int value = input.getExistingValue();
                        switch (input.getPrimitiveValue().charAt(0)) {
                            case '+':
                                levelData.addLevels(value);
                                break;
                            case '-':
                                levelData.removeLevels(-value);
                                break;
                            default:
                                levelData.setLevels(value);
                                break;
                        }

                        // send feedback command
                        LocaleUtils.sendMessage(player, "data-spy.modified.levels",
                                "{previous}", String.valueOf(previous),
                                "{value}", String.valueOf(levelData.getLevels()),
                                "{player}", Objects.requireNonNull(levelData.getPlayer()).getName(),
                                "{level}", levelData.getLevelName());

                        // reopen the menu
                        menu.openMenu(player);
                    },
                    invalid -> LocaleUtils.sendMessage(player, "data-spy.invalid.integer"),
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
        return levelData.getLevels();
    }
}
