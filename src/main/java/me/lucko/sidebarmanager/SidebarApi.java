package me.lucko.sidebarmanager;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public interface SidebarApi {

    /**
     * Registers a sidebar within the manager and creates a new instance
     * @param sidebar the sidebar class to register
     * @param parameters the arguments to pass to the sidebars constructor
     * @return false if the class is already registered or if a new instance could not be made, otherwise true
     */
    boolean registerSidebar(Class<? extends Sidebar> sidebar, Object... parameters);

    /**
     * Registers a sidebar within the manager and creates a new instance
     * @param sidebar the sidebar class to register
     * @return false if the class is already registered or if a new instance could not be made, otherwise true
     */
    boolean registerSidebar(Class<? extends Sidebar> sidebar);

    /**
     * Checks if a sidebar is registered within the manager
     * @param sidebar the sidebar to check
     * @return true if the sidebar is registered
     */
    boolean isRegistered(Class<? extends Sidebar> sidebar);

    /**
     * Sets the default sidebar
     * @param defaultSidebar the sidebar to set
     * @throws IllegalArgumentException if the sidebar has not been registered
     */
    void setDefaultSidebar(Class<? extends Sidebar> defaultSidebar);

    /**
     * Selects a sidebar for a player to view
     * @param player the player to select for
     * @param sidebar the sidebar to select. passing null here removes any active sidebar
     */
    void selectSidebar(Player player, Class<? extends Sidebar> sidebar);

    /**
     * Removes the players active sidebar
     * @param player the player to remove the sidebar from
     */
    void removeSidebar(Player player);

    /**
     * Set the active sidebar of all online players to the default one
     */
    void forceDefaultSidebar();

    /**
     * Gets a players active sidebar
     * @param player the player whose sidebar to get
     * @return the sidebar class the player is viewing
     */
    Class<? extends Sidebar> getActiveSidebar(Player player);

    /**
     * @return a {@link Set} of the sidebars loaded into the manager
     */
    Set<Class<? extends Sidebar>> getLoadedSidebars();

    /**
     * @return a {@link List} of the sidebar instances in the manager
     */
    List<Sidebar> getInstances();

    /**
     * Get a sidebar instance from a class
     * @param sidebar the sidebar class to get an instance for
     * @return a {@link Sidebar} instance
     */
    Sidebar getSidebarInstance(Class<? extends Sidebar> sidebar);

}