package com.github.mlefeb01.spawners.events;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SpawnerMineEvent extends PlayerEvent implements Cancellable {
    private EntityType spawnerType;
    private double tax;
    private double chanceToMine;
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    public SpawnerMineEvent(Player player, EntityType spawnerType, double tax, double chanceToMine) {
        super(player);
        this.isCancelled = false;
        this.spawnerType = spawnerType;
        this.tax = tax;
        this.chanceToMine = chanceToMine;
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

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getChanceToMine() {
        return chanceToMine;
    }

    public void setChanceToMine(double chanceToMine) {
        this.chanceToMine = chanceToMine;
    }

}
