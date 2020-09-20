package com.github.mlefeb01.spawners.listener;

import com.github.mlefeb01.spawners.SpawnerPlugin;
import com.github.mlefeb01.spawners.config.ConfigYml;
import com.github.mlefeb01.spawners.events.SpawnerChangeEvent;
import com.github.mlefeb01.spawners.events.SpawnerExplodeEvent;
import com.github.mlefeb01.spawners.events.SpawnerMineEvent;
import com.github.mlefeb01.spawners.events.SpawnerPlaceEvent;
import com.github.mlefeb01.spawners.util.Constants;
import com.github.mlefeb01.spigotutils.api.utils.ItemUtils;
import com.github.mlefeb01.spigotutils.api.utils.TextUtils;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class SpawnerListener implements Listener {
    private final ConfigYml configYml;
    private final Map<Location, Long> spawnerLifetime;
    private final Economy economy;

    public SpawnerListener(ConfigYml configYml, Map<Location, Long> spawnerLifetime, Economy economy) {
        this.configYml = configYml;
        this.spawnerLifetime = spawnerLifetime;
        this.economy = economy;
    }

    private long calculateLifetime(Block block) {
        if (!configYml.isLifetimeEnabled()) {
            return -1;
        }
        final long lifetime = spawnerLifetime.getOrDefault(block.getLocation(), -1L);
        return lifetime == -1 ? -1 : System.currentTimeMillis() - lifetime;
    }

    private void lifetimeHelper(Location location, boolean put) {
        if (!configYml.isLifetimeEnabled()) {
            return;
        }

        if (put) {
            spawnerLifetime.put(location, System.currentTimeMillis());
        } else {
            spawnerLifetime.remove(location);
        }
    }

    private boolean playerHasSpawner(Player player, EntityType type) {
        final ItemStack targetSpawner = SpawnerPlugin.getSpawnerAPI().createSpawner(type);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(targetSpawner)) {
                return true;
            }
        }

        return false;
    }

    private void spawnMobOnSpawner(Player player, ItemStack playerItem, Block block, BlockFace face) {
        final EntityType type = ItemUtils.getEntityTypeFromSpawnEggId(playerItem.getDurability());

        final Location loc = block.getLocation();
        switch (face) {
            case UP:
                loc.add(0.5, 1, 0.5);
                break;
            case DOWN:
                loc.add(0.5, -1, 0.5);
                break;
            case NORTH:
                loc.add(0.5, 0, -0.5);
                break;
            case EAST:
                loc.add(1.5, 0, 0.5);
                break;
            case SOUTH:
                loc.add(0.5, 0, 1.5);
                break;
            case WEST:
                loc.add(-0.5, 0, 0.5);
                break;
        }

        block.getWorld().spawnEntity(loc, type);

        if (player.getGameMode() == GameMode.SURVIVAL) {
            if (playerItem.getAmount() == 1) {
                player.setItemInHand(null);
            } else {
                playerItem.setAmount(playerItem.getAmount() - 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerMine(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (block.getType() != Material.MOB_SPAWNER || event.isCancelled()) {
            return;
        }

        // Check to see if the require-silk-touch option is enabled
        if (configYml.isRequireSilkTouch()) {
            // Check to see if the player is holding an item with silk touch
            final ItemStack playerItem = player.getItemInHand();
            if (playerItem == null || !playerItem.containsEnchantment(Enchantment.SILK_TOUCH)) {
                lifetimeHelper(block.getLocation(), false);
                return;
            }

            // Check if the player has permission to mine spawners with silk touch
            if (!player.hasPermission(configYml.getMineWithSilkPermission())) {
                event.setCancelled(true);
                player.sendMessage(configYml.getNoPermissionToMineMessage());
                return;
            }
        }

        // Call the SpawnerMineEvent and check if its cancelled
        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
        final EntityType tempType = spawner.getSpawnedType();
        final SpawnerMineEvent spawnerMineEvent = new SpawnerMineEvent(
                player,
                spawner,
                calculateLifetime(block),
                tempType,
                configYml.isSpawnerTax() ? configYml.getTaxPrices().getOrDefault(tempType, 0.0) : 0.0,
                Math.random() * 100
        );
        Bukkit.getPluginManager().callEvent(spawnerMineEvent);
        if (spawnerMineEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // Run % chance to actually drop the spawner from mining it
        if (spawnerMineEvent.getChanceToMine() > configYml.getMiningDropChance()) {
            lifetimeHelper(block.getLocation(), false);
            player.sendMessage(configYml.getFailureMinedSpawnerMessage());
            return;
        }
        event.setCancelled(true);

        // Check if spawner tax is enabled and the spawners entity type is taxed
        boolean withdraw = false;
        if (configYml.isSpawnerTax()) {
            // Check if the player has enough money
            final double balance = economy.getBalance(player);
            if (balance < spawnerMineEvent.getTax()) {
                player.sendMessage(configYml.getNotEnoughMoneyMessage()
                        .replace("%cost%", "" + spawnerMineEvent.getTax())
                        .replace("%balance%", "" + balance)
                );
                return;
            } else {
                withdraw = true;
            }

        }

        // Create the spawner item then check to see whether to add the spawners directly to the players inventory, or drop naturally
        final EntityType type = spawnerMineEvent.getSpawnerType();
        final ItemStack spawnerItem = SpawnerPlugin.getSpawnerAPI().createSpawner((configYml.getTypeWhitelist().contains(tempType) ? type : EntityType.PIG));
        if (configYml.isDropDirectlyToInventory()) {
            if (!playerHasSpawner(player, type) && player.getInventory().firstEmpty() == -1) {
                player.sendMessage(configYml.getInventoryFullMessage());
                return;
            }
            player.getInventory().addItem(spawnerItem);
        } else {

            block.getWorld().dropItem(block.getLocation(), spawnerItem);
        }

        if (withdraw) {
            economy.withdrawPlayer(player, spawnerMineEvent.getTax());
            player.sendMessage(configYml.getHasEnoughMoneyMessage().replace("%cost%", "" + spawnerMineEvent.getTax()));
        }

        // Send a confirmation message to the player that they just mined a spawner, and set the blocks type to air
        player.sendMessage(configYml.getSuccessMinedSpawnerMessage().replace("%type%", TextUtils.formatEnumAsString(type)));
        block.setType(Material.AIR);

        // Lifetime
        lifetimeHelper(block.getLocation(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (block.getType() != Material.MOB_SPAWNER || event.isCancelled()) {
            return;
        }

        // Check to see if spawner lock is enabled
        if (configYml.isSpawnerLock() && !player.hasPermission(configYml.getSpawnerCommandPermission())) {
            player.sendMessage(configYml.getLockEnabledMessage());
            event.setCancelled(true);
            return;
        }

        // Check to see if the spawner has expired
        final NBTItem nbtItem = new NBTItem(player.getItemInHand());
        if (configYml.isExpireEnabled()) {
            // Check if the spawner has an expire time, and the spawner is expired
            final long expireTime = nbtItem.getLong(Constants.NBT_SPAWNER_EXPIRE);
            if (expireTime != -1 && expireTime < System.currentTimeMillis()) {
                player.sendMessage(configYml.getExpiredSpawnerMessage());
                event.setCancelled(true);
                return;
            // Check if the spawner does NOT have an expire time and no expire spawners are blocked
            } else if (expireTime == -1 && configYml.isBlockNoExpireSpawners()) {
                player.sendMessage(configYml.getNoExpireSpanwerBlockedMessage());
                event.setCancelled(true);
                return;
            }
        }

        // Create the SpawnerPlaceEvent and call it. Make sure the event hasnt been cancelled before proceeding
        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
        final EntityType tempType = EntityType.valueOf(nbtItem.hasKey(Constants.NBT_SPAWNER_TYPE) ? nbtItem.getString(Constants.NBT_SPAWNER_TYPE) : "PIG");
        final SpawnerPlaceEvent spawnerPlaceEvent = new SpawnerPlaceEvent(
                player,
                spawner,
                0L,
                tempType
        );
        Bukkit.getPluginManager().callEvent(spawnerPlaceEvent);
        if (spawnerPlaceEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        spawner.setSpawnedType(spawnerPlaceEvent.getSpawnerType());
        spawner.update();

        // Send a confirmation message to the player
        player.sendMessage(configYml.getPlacedSpawnerMessage().replace("%type%", TextUtils.formatEnumAsString(spawnerPlaceEvent.getSpawnerType())));

        // Lifetime
        lifetimeHelper(block.getLocation(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnrExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        /*
        Save the time this explosion occured, so we can use the timestamp to group the spawner items together. If we were
        to not do this, each spawner would have a unique timestamp because of how System.currentTimeMillis() works and it
        would result in the players inventory getting filled too quickly
         */
        final long explodeTime = System.currentTimeMillis();
        for (Block block : event.blockList()) {
            if (block.getType() == Material.MOB_SPAWNER) {
                // Create the SpawnerExplodeEvent and call it
                final SpawnerExplodeEvent spawnerExplodeEvent = new SpawnerExplodeEvent(
                        calculateLifetime(block),
                        ((CreatureSpawner) block.getState()).getSpawnedType(),
                        event.getEntity(),
                        configYml.getSpawnerDropChances().getOrDefault(event.getEntityType(), 0.0)
                );
                Bukkit.getPluginManager().callEvent(spawnerExplodeEvent);

                // Run % chance to actually drop the spawner from explosion
                final double r = ThreadLocalRandom.current().nextDouble(0, 101);
                if (r > spawnerExplodeEvent.getChance()) {
                    continue;
                }

                // Grab the type of mob spawned by said spawner, and new spawner item that matches its type at the blocks location
                block.getWorld().dropItem(block.getLocation(), SpawnerPlugin.getSpawnerAPI().createSpawner(spawnerExplodeEvent.getSpawnerType(), explodeTime));

                // Lifetime
                lifetimeHelper(block.getLocation(), false);
            }
        }
    }

    @EventHandler
    public void onSpawnerInteract(PlayerInteractEvent event) {
        // Make sure the player interaction was a player right clicking a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Make sure the clicked block was a mob spawner
        final Block block = event.getClickedBlock();
        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        // Make sure the player is holding a spawn egg
        final Player player = event.getPlayer();
        final ItemStack playerItem = player.getItemInHand();
        if (playerItem == null || playerItem.getType() == Material.AIR) {
            return;
        }

        if (playerItem.getType() == Material.MONSTER_EGG) {
            event.setCancelled(true);

            // Check if changing spawners via spawn egg is enabled in the config
            if (!configYml.isSpawnerChange()) {
                spawnMobOnSpawner(player, playerItem, block, event.getBlockFace());
                return;
            }

            // Check if the player has permission to change spawners with spawn eggs
            if (!player.hasPermission(configYml.getChangePermission())) {
                spawnMobOnSpawner(player, playerItem, block, event.getBlockFace());
                return;
            }

            // Fire the SpawnerChangeEvent and handle the rest of the event
            final CreatureSpawner spawner = (CreatureSpawner) block.getState();
            final SpawnerChangeEvent changeEvent = new SpawnerChangeEvent(
                    player,
                    spawner,
                    calculateLifetime(block),
                    ItemUtils.getEntityTypeFromSpawnEggId(playerItem.getDurability())
            );
            Bukkit.getPluginManager().callEvent(changeEvent);
            if (changeEvent.isCancelled()) {
                return;
            }

            // Change the spawner type and remove the spawn egg from the player if they are in survival
            spawner.setSpawnedType(changeEvent.getType());
            spawner.update();
            if (player.getGameMode() == GameMode.SURVIVAL) {
                if (playerItem.getAmount() == 1) {
                    player.setItemInHand(null);
                } else {
                    playerItem.setAmount(playerItem.getAmount() - 1);
                }
            }
        } else if (playerItem.getType() == Material.WATCH && !playerItem.hasItemMeta() && configYml.isLifetimeEnabled()) {
            Long time = spawnerLifetime.get(block.getLocation());
            if (time == null) {
                final long temp = System.currentTimeMillis();
                spawnerLifetime.put(block.getLocation(), temp);
                time = temp;
            }

            player.sendMessage(configYml.getLifetimeMessage().replace("%time%", TextUtils.formatSecondsAsTime((int) ((System.currentTimeMillis() - time)/1000))));
        }
    }

}
