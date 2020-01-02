package com.github.mlefeb01.spawners.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerExplodeEvent extends Event {
    private EntityType spawnerType;
    private Entity explodingEntity;
    private double chance;
    private static final HandlerList HANDLERS = new HandlerList();

    public SpawnerExplodeEvent(EntityType spawnerType, Entity explodingEntity, double chance) {
        this.spawnerType = spawnerType;
        this.explodingEntity = explodingEntity;
        this.chance = chance;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public EntityType getSpawnerType() {
        return spawnerType;
    }

    public void setSpawnerType(EntityType spawnerType) {
        this.spawnerType = spawnerType;
    }

    public Entity getExplodingEntity() {
        return explodingEntity;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

}
