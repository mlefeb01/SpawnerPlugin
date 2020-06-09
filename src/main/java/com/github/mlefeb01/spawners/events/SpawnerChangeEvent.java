package com.github.mlefeb01.spawners.events;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Event fired when a player attempts to change a spawner with a mob egg
 */
public class SpawnerChangeEvent extends PlayerSpawnerEvent {
    private EntityType type;

    /**
     * Constructor
     * @param player player
     * @param lifetime spawner lifetime in milliseconds or -1 if the feature is disabled
     * @param spawner spawner
     * @param type entity type
     */
    public SpawnerChangeEvent(Player player, CreatureSpawner spawner, long lifetime,  EntityType type) {
        super(player, spawner, lifetime);
        this.player = player;
        this.type = type;
    }

    /**
     * Getter method for the type the spawner will be set to
     * @return type
     */
    public EntityType getType() {
        return this.type;
    }

    /**
     * Setter method for the type the spawner will be set to
     * @param type type
     */
    public void setType(EntityType type) {
        this.type = type;
    }

}
