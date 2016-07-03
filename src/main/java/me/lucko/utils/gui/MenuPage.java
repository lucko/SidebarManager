package me.lucko.utils.gui;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class MenuPage extends Page {

    @Getter(AccessLevel.PROTECTED)
    private final Map<Integer, ClickableItem> items;

    public MenuPage(String title, Player holder, MenuManager manager, int pageSize) {
        super(title, holder, manager, pageSize);

        items = new HashMap<>();
    }

    public void setItem(int position, Item item) {
        super.setItem(position, item.getItemStack());
        if (item instanceof ClickableItem) {
            items.put(position, (ClickableItem) item);
        }
    }

    public void addItem(Item item) {
        setItem(getFirstEmptySlot(), item);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent e) {
        if (items.keySet().contains(e.getSlot())) {
            items.get(e.getSlot()).onInventoryClick(e, (Player) e.getWhoClicked());
        }
    }

    @Override
    public void removeItem(int i) {
        super.removeItem(i);
        items.remove(i);
    }

    @Override
    public void clearInventory() {
        super.clearInventory();
        items.clear();
    }

}
