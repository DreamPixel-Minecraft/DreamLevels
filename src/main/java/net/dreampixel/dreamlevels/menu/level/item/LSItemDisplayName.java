package net.dreampixel.dreamlevels.menu.level.item;

import lombok.var;
import net.dreampixel.dreamlevels.level.Level;
import net.dreampixel.dreamlevels.menu.level.LevelSpyManager;
import net.dreampixel.dreamlevels.menu.level.menu.LevelModificationMenu;
import net.dreampixel.dreamlevels.util.LocaleUtils;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import top.shadowpixel.shadowcore.api.input.DataInputController;
import top.shadowpixel.shadowcore.api.menu.component.MenuItem;
import top.shadowpixel.shadowcore.util.text.ReplaceUtils;

import java.util.Objects;

public class LSItemDisplayName extends MenuItem {
    private final LevelModificationMenu menu;
    private final Level level;

    private ItemMeta originalMeta;

    public LSItemDisplayName(LevelModificationMenu menu) {
        super(Objects.requireNonNull(LevelSpyManager.getInstance().getItemByKey("display-name")));
        this.menu = menu;
        this.level = menu.getLevel();

        // menu item settings
        updateItem();

        addClickAction(event -> {
            var player = event.getPlayer();
            player.closeInventory();
            LocaleUtils.sendMessages(player, "level-spy.modify.display-name",
                    "{player}", player.getName(),
                    "{level}", level.getName());
            DataInputController.getInstance().<String>createInput()
                    .player(player)
                    .type(String.class)
                    .onInput(input -> {
                        var previous = level.getDisplayName();
                        var value = input.getExistingValue();
                        level.setDisplayName(value);

                        // send feedback message
                        LocaleUtils.sendMessage(player, "level-spy.modified.display-name",
                                "{previous}", String.valueOf(previous),
                                "{value}", value,
                                "{level}", level.getName());

                        // reopen the menu
                        menu.openMenu(player);
                    })
                    .onCancelled(() -> {
                        LocaleUtils.sendMessage(player, "level-spy.cancel");
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

        // replace display name
        meta.setDisplayName(ReplaceUtils.replace(originalMeta.getDisplayName(),
                "{value}", getDisplayValue().toString(),
                "{level}", level.getName()));

        // replace lore
        if (meta.hasLore()) {
            //noinspection DataFlowIssue
            meta.setLore(ReplaceUtils.replace(originalMeta.getLore(),
                    "{value}", getDisplayValue().toString(),
                    "{level}", level.getName()));
        }

        setItemMeta(meta);
        menu.setItem(1, 11, this);
    }

    @NotNull
    public Object getDisplayValue() {
        return level.getDisplayName();
    }
}
