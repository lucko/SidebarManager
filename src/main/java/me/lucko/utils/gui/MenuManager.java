package me.lucko.utils.gui;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MenuManager implements Listener {

    @Getter(AccessLevel.PROTECTED)
    private final Plugin plugin;

    private final Map<UUID, Page> openPages;

    public MenuManager(Plugin plugin) {
        this.plugin = plugin;
        openPages = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, new UpdateTaskSecond(), 0L, 20L);
        plugin.getServer().getScheduler().runTaskTimer(plugin, new UpdateTaskTick(), 0L, 1L);
    }

    public void cleanup() {
        for (UUID u : openPages.keySet()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                p.closeInventory();
            }
        }
        openPages.clear();
    }

    public void getOpenPage(UUID uuid) {
        openPages.get(uuid);
    }

    public void setOpenPage(UUID uuid, Page page) {
        if (page != null) {
            openPages.put(uuid, page);
        } else clearOpenPage(uuid);
    }

    public void clearOpenPage(UUID uuid) {
        openPages.remove(uuid);
    }

    public void clearAllOpenPages() {
        openPages.clear();
    }

    public int getOpenPageCount() {
        return openPages.size();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isViewingPage(UUID uuid) {
        return openPages.containsKey(uuid) && openPages.get(uuid) != null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final HumanEntity h = e.getWhoClicked();
        if (!(h instanceof Player)) return;
        UUID whoClicked = h.getUniqueId();

        // Check if the player is registered within the system
        if (!isViewingPage(whoClicked)) return;

        e.setCancelled(true);
        Page page = openPages.get(whoClicked);
        page.onInventoryClick(e);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        final HumanEntity h = e.getPlayer();
        if (!(h instanceof Player)) return;
        UUID whoClicked = h.getUniqueId();

        // Check if the player is registered within the system
        if (!isViewingPage(whoClicked)) return;
        Page page = openPages.get(whoClicked);
        page.onInventoryClose(e);
    }

    private class UpdateTaskSecond implements Runnable {
        @Override
        public void run() {
            openPages.values().forEach(Page::onUpdateSecond);
        }
    }

    private class UpdateTaskTick implements Runnable {
        @Override
        public void run() {
            openPages.values().forEach(Page::onUpdateTick);
        }
    }
}