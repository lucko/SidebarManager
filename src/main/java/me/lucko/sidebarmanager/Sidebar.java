package me.lucko.sidebarmanager;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.lucko.sidebarmanager.SidebarPlugin.color;

@Getter
@ToString(of = "name")
@EqualsAndHashCode(of = "name")
public abstract class Sidebar {

    /**
     * The name of the sidebar
     */
    private final String name;

    /**
     * The icon that represents this sidebar
     */
    private final Material icon;

    /**
     * A map of all of the users who are viewing this sidebar, and their corresponding scoreboard instances
     */
    private final Map<UUID, Scoreboard> activeScoreboards;

    /**
     * The permission needed to switch to this sidebar
     */
    @Setter
    private String requiredPermission = null;

    /**
     * The default title of the scoreboard
     */
    private final String title;

    public Sidebar(String name, String title, Material icon, String requiredPermission) {
        this.name = name;
        this.title = color(title);
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
}
