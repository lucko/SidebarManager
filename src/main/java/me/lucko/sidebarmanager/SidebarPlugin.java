package me.lucko.sidebarmanager;

import lombok.Getter;
import me.lucko.sidebarmanager.api.ISidebarApi;
import me.lucko.sidebarmanager.api.SidebarApi;
import me.lucko.sidebarmanager.core.SidebarManager;
import me.lucko.utils.gui.MenuManager;
import me.lucko.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class SidebarPlugin extends JavaPlugin {

    private static SidebarApi api = null;

    @Getter
    private SidebarManager manager;

    @Getter
    private MenuManager menuManager;

    public static ISidebarApi getApi() {
        return api;
    }

    @Override
    public void onEnable() {
        if (api == null) {
            api = new SidebarApi();
        }
        api.setPlugin(this);

        manager = new SidebarManager(this, loadRefreshTime());
        menuManager = new MenuManager(this);

        PluginCommand cmd = getCommand("sidebar");
        cmd.setAliases(Arrays.asList("scoreboard", "sb", "toggletb", "togglesidebar"));
        cmd.setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("sidebarmanager.reload")) {
                refresh();
                sender.sendMessage(Util.color("&a&l[Sidebar] &eReload complete."));
                return true;
            }

            if (!sender.hasPermission("scoreboardmanager.change") && !sender.hasPermission("sidebarmanager.change")) {
                sender.sendMessage(Util.color("&a&l[Sidebar] &eYou do not have permission to change your sidebar."));
                return true;
            }

            if (sender instanceof Player) {
                openMenu((Player) sender);
            } else {
                sender.sendMessage(Util.color("&a&l[Sidebar] &eYou need to be a player to use this command."));
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.cleanup();
        }

        if (menuManager != null) {
            menuManager.cleanup();
        }

        manager = null;
        menuManager = null;

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("WeakerAccess")
    public void refresh() {
        manager.rescheduleTasks(this, loadRefreshTime());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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

    public void openMenu(final Player player) {
        new SidebarMenu(player, this).open();
    }
}
