package me.lucko.sidebarmanager;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class SidebarPlugin extends JavaPlugin implements SidebarApi {

    @Getter
    private SidebarManager manager;

    @Override
    public void onEnable() {
        manager = new SidebarManager(this, loadRefreshTime());

        PluginCommand cmd = getCommand("sidebar");
        cmd.setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("sidebarmanager.reload")) {
                refresh();
                sender.sendMessage(color("&a&l[Sidebar] &eReload complete."));
            }
            return true;
        });

        getServer().getServicesManager().register(SidebarApi.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);

        if (manager != null) {
            manager.cleanup();
        }
        manager = null;

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    private void refresh() {
        manager.rescheduleTasks(this, loadRefreshTime());
    }

    private long loadRefreshTime() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        YamlConfiguration config = new YamlConfiguration();
        long refreshTime = 0L;

        try {
            config.load(configFile);
            refreshTime =  config.getLong("refresh-ticks", 0L);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return Math.max(refreshTime, 2L);
    }

    @Override
    public boolean registerSidebar(Class<? extends Sidebar> clazz, Object... parameters) {
        return getManager().registerSidebar(clazz, parameters);
    }

    @Override
    public boolean registerSidebar(Class<? extends Sidebar> clazz) {
        return getManager().registerSidebar(clazz);
    }

    @Override
    public boolean isRegistered(Class<? extends Sidebar> clazz) {
        return getManager().isRegistered(clazz);
    }

    @Override
    public void setDefaultSidebar(Class<? extends Sidebar> clazz) {
        getManager().setDefaultSidebar(clazz);
    }

    @Override
    public void selectSidebar(Player player, Class<? extends Sidebar> clazz) {
        getManager().selectSidebar(player, clazz);
    }

    @Override
    public void removeSidebar(Player player) {
        getManager().removeSidebar(player);
    }

    @Override
    public void forceDefaultSidebar() {
        getManager().forceDefaultSidebar();
    }

    @Override
    public Class<? extends Sidebar> getActiveSidebar(Player player) {
        return getManager().getActiveSidebar(player);
    }

    @Override
    public Set<Class<? extends Sidebar>> getLoadedSidebars() {
        return getManager().getLoadedSidebars();
    }

    @Override
    public List<Sidebar> getInstances() {
        return getManager().getInstances();
    }

    @Override
    public Sidebar getSidebarInstance(Class<? extends Sidebar> clazz) {
        return getManager().getSidebarInstance(clazz);
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
