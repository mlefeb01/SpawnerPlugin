package com.github.mlefeb01.spawners.events;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Event fired when a player mines a spawner and the spawner should be dropped
 */
public final class SpawnerMineEvent extends PlayerSpawnerEvent {
    private EntityType spawnerType;
    private double tax;
    private double chanceToMine;

    /**
     * Constructor
     * @param player player
     * @param spawner spawner
     * @param lifetime spawner lifetime in milliseconds or -1 if the feature is disabled
     * @param spawnerType spawnerType
     * @param tax tax
     * @param chanceToMine chanceToMine
     */
    public SpawnerMineEvent(Player player, CreatureSpawner spawner, long lifetime, EntityType spawnerType, double tax, double chanceToMine) {
        super(player, spawner, lifetime);
        this.spawnerType = spawnerType;
        this.tax = tax;
        this.chanceToMine = chanceToMine;
    }

    /**
     * Getter method for the type of spawner that will be dropped
     * @return type
     */
    public EntityType getSpawnerType() {
        return spawnerType;
    }

    /**
     * Setter method for the type of spawner that will be dropped
     * @param spawnerType type
     */
    public void setSpawnerType(EntityType spawnerType) {
        this.spawnerType = spawnerType;
    }

    /**
     * Getter method for the amount of money it will cost to mine the spawner
     * @return tax
     */
    public double getTax() {
        return tax;
    }

    /**
     * Setter method for the amount of money it will cost to mine the spawner
     * @param tax tax
     */
    public void setTax(double tax) {
        this.tax = tax;
    }

    /**
     * Getter method for the player's chance to successfully mine the spawner (0 - 100 inclusive)
     * @return chance
     */
    public double getChanceToMine() {
        return chanceToMine;
    }

    /**
     * Setter method for the player's chance to successfully mine the spawner (0 - 100 inclusive)
     * @param chanceToMine chance
     */
    public void setChanceToMine(double chanceToMine) {
        this.chanceToMine = chanceToMine;
    }

}
