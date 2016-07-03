package me.lucko.sidebarmanager.core;

import lombok.Getter;
import lombok.Setter;
import me.lucko.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Sidebar {

    /**
     * The name of the sidebar
     */
    @Getter
    private final String name;

    /**
     * The icon that represents this sidebar
     */
    @Getter
    private final Material icon;

    /**
     * A map of all of the users who are viewing this sidebar, and their corresponding scoreboard instances
     */
    @Getter
    private final Map<UUID, Scoreboard> activeScoreboards;

    @Getter
    @Setter
    /**
     * The permission needed to switch to this sidebar
     */
    private String requiredPermission = null;

    /**
     * The default title of the scoreboard
     */
    @Getter
    private final String title;

    public Sidebar(String name, String title, Material icon, String requiredPermission) {
        this.name = name;
        this.title = Util.color(title);
        this.icon = icon;
        this.requiredPermission = requiredPermission;

        activeScoreboards = new HashMap<>();
    }

    public Sidebar(String name, String title, Material icon) {
        this(name, title, icon, null);
    }

    Scoreboard createSidebar(Player player) {
        Scoreboard s = new Scoreboard(this);
        activeScoreboards.put(player.getUniqueId(), s);
        return s;
    }

    public Scoreboard getSidebar(Player player) {
        return activeScoreboards.get(player.getUniqueId());
    }

    void deleteSidebar(Player player) {
        activeScoreboards.remove(player.getUniqueId());
    }

    // Called whenever the update task is ran.
    // A chance to update the values for all players
    protected abstract void onPreUpdateForPlayer(Player player, Scoreboard scoreboard);

    void preUpdateForAllPlayers() {
        activeScoreboards.keySet().stream().filter(u -> Bukkit.getPlayer(u) != null).forEach(u -> preUpdateForPlayer(Bukkit.getPlayer(u)));
    }

    void updateForAllPlayers() {
        activeScoreboards.keySet().stream().filter(u -> Bukkit.getPlayer(u) != null).forEach(u -> updateForPlayer(Bukkit.getPlayer(u)));
    }

    void preUpdateForPlayer(Player player) {
        final Scoreboard sb = activeScoreboards.get(player.getUniqueId());

        if (sb != null) {
            onPreUpdateForPlayer(player, sb);
        }
    }

    void updateForPlayer(Player player) {
        final Scoreboard sb = activeScoreboards.get(player.getUniqueId());

        if (sb != null) {
            sb.update();
            sb.send(player);
        }
    }

    public boolean canUse(Player player) {
        return requiredPermission == null || requiredPermission.equals("") || player.hasPermission(requiredPermission);
    }

    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof Sidebar && name.equals(((Sidebar) obj).getName());
    }
}
