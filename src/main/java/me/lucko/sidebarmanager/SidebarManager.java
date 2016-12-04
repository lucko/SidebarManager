package me.lucko.sidebarmanager;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SidebarManager implements Listener {

    private BukkitTask[] tasks = new BukkitTask[]{null, null};

    /**
     * A map of all loaded sidebars and an instance for each loaded class
     */
    private final Map<Class<? extends Sidebar>, Sidebar> sidebars;

    /**
     * A map of the sidebars each player on the server is using
     * {@link Class} may be null if the player has no active scoreboard
     */
    private final Map<UUID, Class<? extends Sidebar>> activeSidebars;

    /**
     * The default sidebar applied to players when they join the server
     * May be null
     */
    @Getter
    private Class<? extends Sidebar> defaultSidebar = null;

    public SidebarManager(Plugin plugin, long refresh) {
        sidebars = new HashMap<>();
        activeSidebars = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        rescheduleTasks(plugin, refresh);
        forceDefaultSidebar();
    }

    public void rescheduleTasks(Plugin plugin, long refresh) {
        for (BukkitTask t : tasks) {
            if (t != null) {
                t.cancel();
            }
        }

        plugin.getLogger().info("[SBM] Scheduling update tasks to run at an interval of " + refresh + " ticks.");
        tasks[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> getInstances().forEach(Sidebar::updateForAllPlayers), 1L, refresh);
        tasks[1] = Bukkit.getScheduler().runTaskTimer(plugin, () -> getInstances().forEach(Sidebar::preUpdateForAllPlayers), 0L, refresh);
    }

    /**
     * Registers a sidebar within the manager and creates a new instance
     * @param clazz the sidebar class to register
     * @param parameters the arguments to pass to the sidebars constructor
     * @return false if the class is already registered or if a new instance could not be made, otherwise true
     */
    public boolean registerSidebar(Class<? extends Sidebar> clazz, Object... parameters) {
        if (clazz == null) return false;
        if (sidebars.containsKey(clazz)) return false;
        Sidebar sidebar = make(clazz, parameters);

        if (sidebar == null) {
            Bukkit.getLogger().info("[SBM] Failed to register sidebar: " + clazz.getName());
            return false;
        }

        Bukkit.getLogger().info("[SBM] Registered new sidebar: " + sidebar.getName());
        sidebars.put(clazz, sidebar);
        return true;
    }

    private Sidebar make(Class<? extends Sidebar> clazz, Object... parameters) {
        Optional<Constructor> constructor = Optional.empty();

        // Not sure if this is the best/correct way to find the right constructor
        cons:
        for (Constructor c : clazz.getDeclaredConstructors()) {
            if (c.getParameterCount() != parameters.length) {
                continue;
            }

            Class[] parameterTypes = c.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] != parameters[i].getClass()) {
                    continue cons;
                }
            }

            constructor = Optional.of(c);
        }

        if (constructor.isPresent()) {
            try {
                Constructor c = constructor.get();
                c.setAccessible(true);
                return (Sidebar) c.newInstance(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("No constructor for class " + clazz.getName() + " could be found with parameters "
                    + Arrays.toString(parameters));
        }
        return null;
    }

    /**
     * Checks if a sidebar is registered within the manager
     * @param clazz the sidebar to check
     * @return true if the sidebar is registered
     */
    public boolean isRegistered(Class<? extends Sidebar> clazz) {
        return sidebars.containsKey(clazz);
    }

    /**
     * Sets the default sidebar
     * @param sidebar the sidebar to set
     */
    public void setDefaultSidebar(Class<? extends Sidebar> sidebar) {
        if (sidebar != null) {
            if (!isRegistered(sidebar)) {
                throw new IllegalArgumentException("Sidebar '"  + sidebar.getSimpleName() + "' cannot be set as default as it is not registered.");
            }
            Bukkit.getLogger().info("[SBM] Set default sidebar to: " + sidebar.getSimpleName());
            defaultSidebar = sidebar;
        } else {
            Bukkit.getLogger().info("[SBM] Set the default sidebar to: null");
            defaultSidebar = null;
        }
    }

    /**
     * Selects a sidebar for a player to view
     * @param player the player to select for
     * @param sidebar the sidebar to select. passing null here removes any active sidebar
     */
    public void selectSidebar(Player player, Class<? extends Sidebar> sidebar) {
        removeSidebar(player);
        if (sidebar != null) {
            activeSidebars.put(player.getUniqueId(), sidebar);
            Sidebar s = getSidebarInstance(sidebar);
            if (s != null) {
                s.createSidebar(player);
                s.preUpdateForPlayer(player);
                s.updateForPlayer(player);
            }
        }
    }

    /**
     * Removes the players active sidebar
     * @param player the player to remove the sidebar from
     */
    public void removeSidebar(Player player) {
        Class<? extends Sidebar> previous = activeSidebars.get(player.getUniqueId());
        if (previous != null) {
            Sidebar s = getSidebarInstance(previous);
            if (s != null) {
                s.deleteSidebar(player);
            }
        }

        activeSidebars.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Set the active sidebar of all online players to the default one
     */
    public void forceDefaultSidebar() {
        Bukkit.getOnlinePlayers().forEach(this::applyDefaultSidebar);
    }

    /**
     * Sets a players active sidebar to the default one
     * @param player the player to set
     */
    private void applyDefaultSidebar(Player player) {
        if (defaultSidebar != null) {
            selectSidebar(player, defaultSidebar);
        } else {
            removeSidebar(player);
        }
    }

    /**
     * Cleans up the instance
     */
    public void cleanup() {
        Bukkit.getOnlinePlayers().forEach(this::removeSidebar);
        sidebars.clear();
        activeSidebars.clear();
        defaultSidebar = null;
    }

    /**
     * @return a {@link Set} of the sidebars loaded into the manager
     */
    public Set<Class<? extends Sidebar>> getLoadedSidebars() {
        return Collections.unmodifiableSet(sidebars.keySet());
    }

    /**
     * @return a {@link List} of the sidebar instances in the manager
     */
    public List<Sidebar> getInstances() {
        return sidebars.keySet().stream().map(this::getSidebarInstance).collect(Collectors.toList());
    }

    /**
     * @return a {@link Map} of player active sidebars
     */
    @SuppressWarnings("unused")
    public Map<UUID, Class<? extends Sidebar>> getActiveSidebars() {
        return Collections.unmodifiableMap(activeSidebars);
    }

    /**
     * Get a sidebar instance from a class
     * @param clazz the sidebar class to get an instance for
     * @return a sidebar instance
     */
    public Sidebar getSidebarInstance(Class<? extends Sidebar> clazz) {
        return sidebars.get(clazz);
    }

    /**
     * Gets a players active sidebar
     * @param player the player whose sidebar to get
     * @return the sidebar the player is viewing
     */
    public Class<? extends Sidebar> getActiveSidebar(Player player) {
        return activeSidebars.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        applyDefaultSidebar(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        removeSidebar(e.getPlayer());
    }
}
