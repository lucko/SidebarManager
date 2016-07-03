package me.lucko.sidebarmanager.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Getter;
import me.lucko.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Scoreboard {

    @Getter
    private final org.bukkit.scoreboard.Scoreboard scoreboard;
    private final Map<String, Integer> values;
    private final List<Integer> toRemove;
    private final Set<String> toUpdate;
    private final List<Team> teams;
    private String title;
    private Objective objective;

    public Scoreboard(Sidebar parent) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.values = new ConcurrentHashMap<>();
        this.toRemove = Lists.newArrayList();
        this.toUpdate = Collections.synchronizedSet(new HashSet<>());
        this.teams = Collections.synchronizedList(Lists.newArrayList());
        this.title = Util.color(parent.getTitle());
    }

    public void add(String text, Integer score) {
        text = Util.color(text);

        if (remove(score, text, false) || !values.containsValue(score)) {
            toUpdate.add(text);
        }

        values.put(text, score);
    }

    public boolean remove(Integer score, String text) {
        return remove(score, text, true);
    }

    public boolean remove(Integer score, String s, boolean b) {
        String toRemove = get(score, s);
        if (toRemove == null) return false;

        values.remove(toRemove);

        if (b) this.toRemove.add(score);
        return true;
    }

    public String get(int score, String s) {
        String str = null;

        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (entry.getValue().equals(score) && !entry.getKey().equals(s)) {
                str = entry.getKey();
            }
        }

        return str;
    }

    public void update() {
        if (toUpdate.isEmpty()) return;

        if (objective == null) {
            objective = scoreboard.registerNewObjective((title.length() > 16 ? title.substring(0, 15) : title), "dummy");
            objective.setDisplayName(title);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        toRemove.stream().forEach((remove) -> {
            for (String s : scoreboard.getEntries()) {
                Score score = objective.getScore(s);
                if (score == null) continue;
                if (score.getScore() != remove) continue;
                scoreboard.resetScores(s);
            }
        });
        toRemove.clear();

        int index = values.size();

        for (Map.Entry<String, Integer> text : values.entrySet()) {
            Team t = scoreboard.getTeam(ChatColor.values()[text.getValue()].toString());
            Map.Entry<Team, String> team;

            if (!toUpdate.contains(text.getKey())) continue;

            if (t != null) {
                String color = ChatColor.values()[text.getValue()].toString();

                team = new AbstractMap.SimpleEntry<>(t, color);
                applyText(team.getKey(), text.getKey(), team.getValue());
                index -= 1;
                continue;

            } else {
                team = createTeam(text.getKey(), text.getValue());
            }

            Integer score = text.getValue() != null ? text.getValue() : index;

            objective.getScore(team.getValue()).setScore(score);
            index -= 1;
        }

        toUpdate.clear();
    }

    public void setTitle(String s) {
        title = Util.color(s);
        if (objective != null) objective.setDisplayName(title);
    }

    public void reset() {
        teams.forEach(Team::unregister);
        teams.clear();
        values.clear();
    }

    public void send(Player... players) {
        for (Player p : players) {
            p.setScoreboard(scoreboard);
        }
    }

    private Map.Entry<Team, String> createTeam(String text, int pos) {
        Team team;
        String result = (ChatColor.values()[pos]).toString();

        try {
            team = scoreboard.registerNewTeam("text-" + (teams.size() + 1));
        } catch (IllegalArgumentException e) {
            team = scoreboard.getTeam("text-" + (teams.size()));
        }

        applyText(team, text, result);

        teams.add(team);
        return new AbstractMap.SimpleEntry<>(team, result);
    }

    private void applyText(Team team, String text, String result) {
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        String prefix = iterator.next();

        team.setPrefix(prefix);

        if (!team.hasEntry(result)) {
            team.addEntry(result);
        }

        if (text.length() <= 16) return;

        // Text is longer than 16 chars
        String prefixColor = ChatColor.getLastColors(prefix);
        String suffix = iterator.next();

        if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            prefix = prefix.substring(0, prefix.length() - 1);
            team.setPrefix(prefix);

            prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
            suffix = suffix.substring(1);
        }

        if (prefixColor == null) prefixColor = "";

        String color = prefixColor.equals("") ? ChatColor.RESET.toString() : prefixColor;

        if (suffix.length() > 16 - color.length()) {
            suffix = suffix.substring(0, 16 - color.length());
        }

        team.setSuffix(color + suffix);
    }
}
