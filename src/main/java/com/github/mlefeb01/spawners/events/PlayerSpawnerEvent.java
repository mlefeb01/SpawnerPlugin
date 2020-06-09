package com.github.mlefeb01.spawners.events;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Parent event for spawner events that involve a player and a cancellable action (e.g. - place, mine, change, etc.)
 */
public abstract class PlayerSpawnerEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final long lifetime;
    private final CreatureSpawner spawner;

    /**
     * Constructor
     * @param player player
     * @param spawner the spawner involved with the event
     * @param lifetime spawner lifetime in milliseconds or -1 if the feature is disabled
     */
    public PlayerSpawnerEvent(Player player, CreatureSpawner spawner, long lifetime) {
        super(player);
        this.spawner = spawner;
        this.lifetime = lifetime;
    }

    /**
     * Check if the event has been cancelled
     * @return boolean cancelled
     */
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Set the evennt's cancelled status
     * @param isCancelled isCancelled
     */
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
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
     * Getter method for the spawner involved with the event
     * @return spawner
     */
    public CreatureSpawner getSpawner() {
        return spawner;
    }

    /**
     * Getter method for the spawner's lifetime
     * @return spawner lifetime in milliseconds or -1 if the feature is disabled
     */
    public long getSpawnerLifetime() {
        return this.lifetime;
    }

}
