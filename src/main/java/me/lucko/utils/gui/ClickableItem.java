package me.lucko.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("WeakerAccess")
public abstract class ClickableItem extends Item {

    public ClickableItem(ItemStack itemStack) {
        super(itemStack);
    }

    public abstract void onInventoryClick(InventoryClickEvent event, Player whoClicked);

}
