package com.github.mlefeb01.spawners.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Utils {

    public static String color(String str) {
        return str == null ? str : ChatColor.translateAlternateColorCodes('&', str);
    }

    public static List<String> colorList(List<String> list) {
        for (int n = 0; n < list.size(); n++) {
            list.set(n, color(list.get(n)));
        }
        return list;
    }

    public static String capitalizeFully(String str) {
        final String[] splitString = str.split(" ");
        for (int n = 0; n < splitString.length; n++) {
            final String temp = splitString[n];
            splitString[n] = (temp.length() > 1) ? temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase() : temp.toUpperCase();
        }
        return String.join(" ", splitString);
    }

    // either gives the player the item, or if their inventory is full drops the item at their location
    public static void safeItemGive(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
    }

}
