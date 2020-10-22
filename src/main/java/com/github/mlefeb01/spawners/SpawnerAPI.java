package com.github.mlefeb01.spawners;

import com.github.mlefeb01.spawners.config.ConfigYml;
import com.github.mlefeb01.spawners.util.Constants;
import com.github.mlefeb01.spigotutils.api.utils.TextUtils;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public final class SpawnerAPI {
    private final ConfigYml configYml;
    private final Map<Location, Long> spawnerLifetime;

    SpawnerAPI(ConfigYml configYml, Map<Location, Long> spawnerLifetime) {
        this.configYml = configYml;
        this.spawnerLifetime = spawnerLifetime;
    }

    public long getSpawnerLifetime(Location location) {
        return getSpawnerLifetime(location.getBlock());
    }

    public long getSpawnerLifetime(Block block) {
        // Check if the lifetime feature is enabled
        if (!configYml.isExpireEnabled()) {
            return -1;
        }

        // Check for blocks that are not a spawner
        final Long time = spawnerLifetime.get(block.getLocation());
        if (block.getType() != Material.MOB_SPAWNER) {
            // Remove the location from the cache if for some reason the block is no longer a spawner (e.g. - worldedit)
            if (time != null) {
                spawnerLifetime.remove(block.getLocation());
            }
            return -1;
        }

        // By now it is known that the block is a spawner, so if it does not have a lifetime cached give it one (e.g. - worldedit)
        if (time == null) {
            spawnerLifetime.put(block.getLocation(), System.currentTimeMillis());
            return 0;
        } else {
            return System.currentTimeMillis() - time;
        }
    }

    private long calculateExpireTime(long startTime) {
        final long expireTime = configYml.isExpireEnabled() ? startTime + (configYml.getExpireTimeLimit() * 1000) : -1;
        if (expireTime != -1 && configYml.isRoundNearestHour()) {
            return expireTime - (expireTime % 3600000);
        }
        return expireTime;
    }

    public ItemStack createSpawner(EntityType spawned, long expireStartTime) {
        final ItemStack spawner = new ItemStack(Material.MOB_SPAWNER, 1);
        final ItemMeta meta = spawner.getItemMeta();

        final long expireTime = calculateExpireTime(expireStartTime);

        meta.setDisplayName(configYml.getSpawnerItemName().replace("%type%", TextUtils.formatEnumAsString(spawned)));
        final String expirePlaceholder = configYml.getExpireTimeFormat().format(new Date(expireTime));
        meta.setLore(configYml.getSpawnerItemLore().stream().map(str -> str.replace("%time%", expireTime == -1 ? "N/A" : expirePlaceholder)).collect(Collectors.toList()));
        spawner.setItemMeta(meta);

        final NBTItem finalSpawner = new NBTItem(spawner);
        finalSpawner.setString(Constants.NBT_SPAWNER_TYPE, spawned.name());
        finalSpawner.setLong(Constants.NBT_SPAWNER_EXPIRE, expireTime);
        return finalSpawner.getItem();
    }

    public ItemStack createSpawner(EntityType spawned) {
        return createSpawner(spawned, System.currentTimeMillis());
    }

    public boolean isCustomSpawner(ItemStack item) {
        return item != null && new NBTItem(item).hasKey(Constants.NBT_SPAWNER_TYPE);
    }

}
