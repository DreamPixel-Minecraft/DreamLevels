package net.dreampixel.dreamlevels.menu.dataspy.item;

import lombok.var;
import net.dreampixel.dreamlevels.data.level.LevelData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataMenu;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import top.shadowpixel.shadowcore.api.input.DataInputController;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;

public class DSItemReset extends MenuItem {
    private final LevelData levelData;

    public DSItemReset(LevelDataMenu menu) {
        super(Objects.requireNonNull(DataSpyManager.getInstance().getItemByKey("reset")));
        this.levelData = menu.getLevel().getLevelData(menu.getUniqueId());

        if (levelData == null) {
            return;
        }

        // menu item settings
        updateItem();

        addClickAction(event -> {
            event.setCancelled(true);
            var player = event.getPlayer();
            player.closeInventory();

            // send tip message
            LocaleUtils.sendMessages(player, "data-spy.modify.reset",
                    "{player}", player.getName(),
                    "{level}", levelData.getLevelName());

            // ask for input content
            DataInputController.getInstance().createInput(player, String.class,
                    input -> {
                        if (input.getExistingValue().equalsIgnoreCase("confirm")) {
                            // reset and send feedback
                            levelData.reset();
                            LocaleUtils.sendMessage(player, "data-spy.modified.reset",
                                    "{player}", Objects.requireNonNull(levelData.getPlayer()).getName(),
                                    "{level}", levelData.getLevelName());

                            // reopen the menu
                            menu.openMenu(player);
                            return;
                        }

                        // send failure feedback and ask to reinput
                        LocaleUtils.sendMessage(player, "data-spy.invalid.text");
                        input.reinput();
                    },
                    i -> {},
                    () -> {
                        // send cancel message and reopen the menu
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

        var player = levelData.getPlayer();
        if (player == null) {
            return;
        }
        
        // replace display name
        meta.setDisplayName(ReplaceUtils.coloredReplace(meta.getDisplayName(), player,
                "{player}", player.getName(),
                "{level}", levelData.getLevelName()));
        // replace lore
        if (meta.hasLore()) {
            //noinspection DataFlowIssue
            meta.setLore(ReplaceUtils.coloredReplace(meta.getLore(), player,
                    "{player}", player.getName(),
                    "{level}", levelData.getLevelName()));
        }

        setItemMeta(meta);
    }
}
