package me.lucko.sidebarmanager.api;

import lombok.Setter;
import me.lucko.sidebarmanager.SidebarPlugin;
import me.lucko.sidebarmanager.core.Sidebar;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

@Setter
public class SidebarApi implements ISidebarApi {
    private SidebarPlugin plugin;

    @Override
    public boolean registerSidebar(Class<? extends Sidebar> clazz, Object... parameters) {
        return plugin.getManager().registerSidebar(clazz, parameters);
    }

    @Override
    public boolean registerSidebar(Class<? extends Sidebar> clazz) {
        return plugin.getManager().registerSidebar(clazz);
    }

    @Override
    public boolean isRegistered(Class<? extends Sidebar> clazz) {
        return plugin.getManager().isRegistered(clazz);
    }

    @Override
    public void setDefaultSidebar(Class<? extends Sidebar> clazz) {
        plugin.getManager().setDefaultSidebar(clazz);
    }

    @Override
    public void selectSidebar(Player player, Class<? extends Sidebar> clazz) {
        plugin.getManager().selectSidebar(player, clazz);
    }

    @Override
    public void removeSidebar(Player player) {
        plugin.getManager().removeSidebar(player);
    }

    @Override
    public void forceDefaultSidebar() {
        plugin.getManager().forceDefaultSidebar();
    }

    @Override
    public Class<? extends Sidebar> getActiveSidebar(Player player) {
        return plugin.getManager().getActiveSidebar(player);
    }

    @Override
    public Set<Class<? extends Sidebar>> getLoadedSidebars() {
        return plugin.getManager().getLoadedSidebars();
    }

    @Override
    public List<Sidebar> getInstances() {
        return plugin.getManager().getInstances();
    }

    @Override
    public Sidebar getSidebarInstance(Class<? extends Sidebar> clazz) {
        return plugin.getManager().getSidebarInstance(clazz);
    }

    @Override
    public void showSidebarMenu(Player player) {
        plugin.openMenu(player);
    }
}
