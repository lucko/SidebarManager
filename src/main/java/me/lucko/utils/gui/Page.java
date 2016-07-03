package me.lucko.utils.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "EmptyMethod"})
public abstract class Page {

    private final Inventory inventory;

    @Getter
    private final String title;

    @Getter
    private final UUID holder;

    @Getter
    private final MenuManager manager;

    @Getter
    @Setter
    private PageBuilder fallbackPage = null;

    public Page(String title, Player holder, MenuManager manager, int pageSize) {
        this.title = title;
        this.holder = holder.getUniqueId();
        this.manager = manager;
        inventory = Bukkit.createInventory(holder, pageSize, title);
    }

    public abstract void onInventoryClick(InventoryClickEvent e);

    public void onInventoryClose(InventoryCloseEvent e) {
        inventory.clear();
        manager.clearOpenPage(holder);

        if (fallbackPage == null) return;
        fallbackPage.buildAndOpen((Player) e.getPlayer());
    }

    public void setup() {

    }

    public void onUpdateSecond() {

    }

    public void onUpdateTick() {

    }

    public int getFirstEmptySlot() {
        return inventory.firstEmpty();
    }

    public void setItem(int i, ItemStack is) {
        inventory.setItem(i, is);
    }

    public void addItem(ItemStack is) {
        inventory.addItem(is);
    }

    public void removeItem(int i) {
        inventory.clear(i);
    }

    public void clearInventory() {
        inventory.clear();
    }

    public void open() {
        final Page instance = this;
        final UUID h = holder;
        // Run at a delay to prevent an inventory being closed and opened in the same tick
        new BukkitRunnable() {
            @Override
            public void run() {
                Player holder = Bukkit.getPlayer(h);
                if (holder == null) return;

                holder.openInventory(inventory);
                manager.setOpenPage(holder.getUniqueId(), instance);
            }
        }.runTaskLater(getManager().getPlugin(), 1L);
    }

    @Override
    public int hashCode() {
        return inventory.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Page && inventory.equals(((Page) obj).inventory);
    }
}
