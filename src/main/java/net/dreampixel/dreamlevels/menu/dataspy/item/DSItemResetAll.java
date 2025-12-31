package net.dreampixel.dreamlevels.menu.dataspy.item;

import lombok.var;
import net.dreampixel.dreamlevels.data.DataManager;
import net.dreampixel.dreamlevels.data.player.PlayerData;
import net.dreampixel.dreamlevels.menu.dataspy.DataSpyManager;
import net.dreampixel.dreamlevels.menu.dataspy.menu.LevelDataOverallMenu;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.Bukkit;
import top.shadowpixel.shadowcore.api.input.DataInputController;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;

public class DSItemResetAll extends MenuItem {
    private final PlayerData playerData;

    public DSItemResetAll(LevelDataOverallMenu menu) {
        super(Objects.requireNonNull(DataSpyManager.getInstance().getItemByKey("reset-all")));
        this.playerData = DataManager.getInstance().getPlayerData(menu.getUniqueId());

        if (playerData == null) {
            return;
        }

        // menu item settings
        updateItem();

        addClickAction(event -> {
            event.setCancelled(true);
            var player = event.getPlayer();
            player.closeInventory();

            // send tip message
            LocaleUtils.sendMessages(player, "data-spy.modify.reset-all",
                    "{player}", player.getName());

            // ask for input content
            DataInputController.getInstance().createInput(player, String.class,
                    input -> {
                        if (input.getExistingValue().equalsIgnoreCase("confirm")) {
                            // reset all data and send feedback
                            playerData.resetAll();
                            LocaleUtils.sendMessage(player, "data-spy.modified.reset-all",
                                    "{player}", Objects.requireNonNull(Bukkit.getPlayer(playerData.getUniqueId())).getName());

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

        var player = Bukkit.getPlayer(playerData.getUniqueId());
        if (player == null) {
            return;
        }

        // replace display name
        meta.setDisplayName(ReplaceUtils.replace(meta.getDisplayName(),
                "{player}", player.getName()));
        // replace lore
        if (meta.hasLore()) {
            //noinspection DataFlowIssue
            meta.setLore(ReplaceUtils.replace(meta.getLore(),
                    "{player}", player.getName()));
        }

        setItemMeta(meta);
    }
}
