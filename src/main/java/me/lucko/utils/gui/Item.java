package me.lucko.utils.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("WeakerAccess")
@AllArgsConstructor
public class Item {

    @Getter
    private final ItemStack itemStack;

    @Override
    public int hashCode() {
        return itemStack.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Item && itemStack.equals(((Item) obj).getItemStack());
    }

}
