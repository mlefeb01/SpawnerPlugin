package com.github.mlefeb01.spawners.events;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SpawnerPlaceEvent extends PlayerEvent implements Cancellable {
    private Player player;
    private EntityType spawnerType;
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    public SpawnerPlaceEvent(Player player, EntityType spawnerType) {
        super(player);
        this.player = player;
        this.isCancelled = false;
        this.spawnerType = spawnerType;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
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

}
