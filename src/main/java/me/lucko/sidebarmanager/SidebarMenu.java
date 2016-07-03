package me.lucko.sidebarmanager;

import me.lucko.sidebarmanager.core.Sidebar;
import me.lucko.utils.gui.ClickableItem;
import me.lucko.utils.gui.ItemStackBuilder;
import me.lucko.utils.gui.MenuPage;
import me.lucko.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

class SidebarMenu extends MenuPage {

    private final SidebarPlugin plugin;

    SidebarMenu(Player holder, SidebarPlugin plugin) {
        super("Sidebar Selector", holder, plugin.getMenuManager(), Util.getMenuSize(plugin.getManager().getLoadedSidebars().size()) + 9);
        this.plugin = plugin;
        setup();
    }

    @Override
    public void setup() {
        final Player player = Bukkit.getPlayer(getHolder());
        if (player == null) return;

        for (final Class<? extends Sidebar> sidebar : plugin.getManager().getLoadedSidebars()) {
            Sidebar asb = plugin.getManager().getSidebarInstance(sidebar);
            if (!asb.canUse(player)) continue;
            if (plugin.getManager().getActiveSidebar(player) != null && plugin.getManager().getActiveSidebar(player).equals(sidebar)) {
                addItem(new ItemStackBuilder(asb.getIcon())
                        .withEnchantment(Enchantment.SILK_TOUCH)
                        .withHiddenEnchants()
                        .withName(Util.color("&b" + asb.getName()))
                        .withLore(Util.color("&eThis is your active sidebar!"))
                        .build());
            } else {
                addItem(new ClickableItem(new ItemStackBuilder(asb.getIcon())
                        .withName(Util.color("&b" + asb.getName()))
                        .withLore(Util.color("&fClick to select this sidebar!"))
                        .build()) {
                    @Override
                    public void onInventoryClick(InventoryClickEvent event, Player whoClicked) {
                        plugin.getManager().selectSidebar(whoClicked, sidebar);
                        whoClicked.sendMessage(Util.color("&a&l[Sidebar] &eSwitched your active sidebar to: " + asb.getName()));
                        whoClicked.closeInventory();
                    }
                });
            }
        }

        setItem(Util.getMenuSize(plugin.getManager().getLoadedSidebars().size()) + 8, new ClickableItem(new ItemStackBuilder(Material.BARRIER)
                .withName(Util.color("&cDisable Sidebar"))
                .withLore(Util.color("&fClick to disable the sidebar."))
                .build()) {
            @Override
            public void onInventoryClick(InventoryClickEvent event, Player whoClicked) {
                plugin.getManager().selectSidebar(whoClicked, null);
                whoClicked.sendMessage(Util.color("&a&l[Sidebar] &eYour active sidebar was disabled."));
                whoClicked.closeInventory();
            }
        });
    }
}
