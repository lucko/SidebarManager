package me.lucko.utils;

import org.bukkit.ChatColor;

public class Util {

    private Util(){}

    public static int getMenuSize(int count) {
        return (count / 9 + ((count % 9 != 0) ? 1 : 0)) * 9;
    }
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
