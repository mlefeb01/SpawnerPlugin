package com.github.mlefeb01.spawners.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
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

    public static EntityType getEntityTypeFromSpawnEgg(int id) {
        switch (id) {
            case 120:
                return EntityType.VILLAGER;
            case 100:
                return EntityType.HORSE;
            case 96:
                return EntityType.MUSHROOM_COW;
            case 67:
                return EntityType.ENDERMITE;
            case 62:
                return EntityType.MAGMA_CUBE;
            case 60:
                return EntityType.SILVERFISH;
            case 91:
                return EntityType.SHEEP;
            case 93:
                return EntityType.CHICKEN;
            case 92:
                return EntityType.COW;
            case 61:
                return EntityType.BLAZE;
            case 59:
                return EntityType.CAVE_SPIDER;
            case 57:
                return EntityType.PIG_ZOMBIE;
            case 51:
                return EntityType.SKELETON;
            case 101:
                return EntityType.RABBIT;
            case 68:
                return EntityType.GUARDIAN;
            case 94:
                return EntityType.SQUID;
            case 95:
                return EntityType.WOLF;
            case 98:
                return EntityType.OCELOT;
            case 66:
                return EntityType.WITCH;
            case 65:
                return EntityType.BAT;
            case 58:
                return EntityType.ENDERMAN;
            case 50:
                return EntityType.CREEPER;
            case 55:
                return EntityType.SLIME;
            case 54:
                return EntityType.ZOMBIE;
            default:
                return EntityType.PIG;
        }
    }

}
