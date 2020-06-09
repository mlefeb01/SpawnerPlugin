package com.github.mlefeb01.spawners.events;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Event fired when a player places a spawner
 */
public class SpawnerPlaceEvent extends PlayerSpawnerEvent {
    private EntityType spawnerType;

    /**
     * Constructor
     * @param player player
     * @param spawner spawner
     * @param lifetime always 0 or -1 if the feature is disabled
     * @param spawnerType spawnerType
     */
    public SpawnerPlaceEvent(Player player, CreatureSpawner spawner, long lifetime, EntityType spawnerType) {
        super(player, spawner, lifetime);
        this.spawnerType = spawnerType;
    }

    /**
     * Getter method for the type of spawner being placed
     * @return type
     */
    public EntityType getSpawnerType() {
        return spawnerType;
    }

    /**
     * Setter method for the type of spawner being placed
     * @param spawnerType
     */
    public void setSpawnerType(EntityType spawnerType) {
        this.spawnerType = spawnerType;
    }

}
