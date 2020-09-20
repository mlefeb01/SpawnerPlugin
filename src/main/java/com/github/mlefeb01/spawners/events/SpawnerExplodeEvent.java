package com.github.mlefeb01.spawners.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a spawner is destroyed via explosion (e.g. - TNT, creeper, etc.)
 */
public final class SpawnerExplodeEvent extends Event {
    private EntityType spawnerType;
    private final Entity explodingEntity;
    private double chance;
    private final long lifetime;
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Constructor
     * @param lifetime spawner lifetime in milliseconds or -1 if the feature is disabled
     * @param spawnerType spawnerType
     * @param explodingEntity explodingEntity
     * @param chance chance
     */
    public SpawnerExplodeEvent(long lifetime, EntityType spawnerType, Entity explodingEntity, double chance) {
        this.spawnerType = spawnerType;
        this.lifetime = lifetime;
        this.explodingEntity = explodingEntity;
        this.chance = chance;
    }

    /**
     * Getter method for the events HandlerList
     * @return HandlerList HANDLERS
     */
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Getter method for the events HandlerList
     * @return HandlerList HANDLERS
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Getter method for the type of spawner being dropped
     * @return
     */
    public EntityType getSpawnerType() {
        return spawnerType;
    }

    /**
     * Setter method for the type of spawner being dropped
     * @param spawnerType
     */
    public void setSpawnerType(EntityType spawnerType) {
        this.spawnerType = spawnerType;
    }

    /**
     * Getter method for the entity that exploded and caused this spawner to break
     * @return
     */
    public Entity getExplodingEntity() {
        return explodingEntity;
    }

    /**
     * Getter method for the chance that this spawner drops as an {@link org.bukkit.inventory.ItemStack}
     * @return
     */
    public double getChance() {
        return chance;
    }

    /**
     * Setter method for the chance that this spawner drops as an {@link org.bukkit.inventory.ItemStack}
     * @param chance
     */
    public void setChance(double chance) {
        this.chance = chance;
    }

    /**
     * Getter method for the spawner lifetime
     * @return lifetime in milliseconds or -1 if the feature is disabled
     */
    public long getSpawnerLifetime() {
        return lifetime;
    }
}
