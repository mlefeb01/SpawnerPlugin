package com.github.mlefeb01.spawners.handlers;

import com.github.mlefeb01.spawners.DataManager;
import com.github.mlefeb01.spawners.FileManager;
import com.github.mlefeb01.spawners.events.SpawnerChangeEvent;
import com.github.mlefeb01.spawners.events.SpawnerExplodeEvent;
import com.github.mlefeb01.spawners.events.SpawnerMineEvent;
import com.github.mlefeb01.spawners.events.SpawnerPlaceEvent;
import com.github.mlefeb01.spawners.utils.Utils;
import de.tr7zw.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SpawnerHandler implements Listener, CommandExecutor {
    private static final String NBT_SPAWNER_TYPE = "karismic.spawner.type";
    private static final String NBT_SPAWNER_EXPIRE = "karismic.spawner.expire";
    private final FileManager fileManager;
    private final DataManager dataManager;
    private final Set<EntityType> spawnerTypes;
    private final FileConfiguration config;
    private final Economy economy;
    private final Map<EntityType, Double> spawnerTax;
    private final Map<EntityType, Double> dropChances;
    private final SimpleDateFormat expireFormat;
    private final Map<Location, Long> spawnerLifetime;

    public SpawnerHandler(FileManager fileManager, DataManager dataManager, Set<EntityType> spawnerTypes, FileConfiguration config,
                          Economy economy, Map<EntityType, Double> spawnerTax, Map<EntityType, Double> dropChances, SimpleDateFormat expireFormat,
                          Map<Location, Long> spawnerExistence) {
        this.fileManager = fileManager;
        this.dataManager = dataManager;
        this.spawnerTypes = spawnerTypes;
        this.config = config;
        this.economy = economy;
        this.spawnerTax = spawnerTax;
        this.dropChances = dropChances;
        this.expireFormat = expireFormat;
        this.spawnerLifetime = spawnerExistence;
    }

    private long calculateExpireTime(long startTime) {
        final long expireTime = config.getBoolean("spawners.expire.enabled") ?
                startTime + (config.getInt("spawners.expire.time-limit") * 1000) : -1;
        if (expireTime != -1 && config.getBoolean("spawners.expire.round-nearest-hour")) {
            return expireTime - (expireTime % 3600000);
        }
        return expireTime;
    }

    private long calculateLifetime(Block block) {
        if (!config.getBoolean("spawners.lifetime")) {
            return -1;
        }
        final long lifetime = spawnerLifetime.getOrDefault(block.getLocation(), -1L);
        return lifetime == -1 ? -1 : System.currentTimeMillis() - lifetime;
    }

    private void lifetimeHelper(Location location, boolean put) {
        if (!config.getBoolean("spawners.lifetime")) {
            return;
        }

        if (put) {
            spawnerLifetime.put(location, System.currentTimeMillis());
        } else {
            spawnerLifetime.remove(location);
        }
    }

    /*
    Formats an entity types name like...
    IRON_GOLEM -> Iron Golem
    PIG -> Pig
     */
    private String formatEntityName(EntityType type) {
        return Utils.capitalizeFully(type.name().replace("_", " ").toLowerCase());
    }

    // Formats the list of entity type into a readable format [pig, cow, ...]
    private String formatEntityTypes() {
        return ChatColor.DARK_GRAY + "[" + spawnerTypes.stream().map(type -> ChatColor.YELLOW + type.toString())
                .collect(Collectors.joining(ChatColor.DARK_GRAY + ", ")) + ChatColor.DARK_GRAY + "]";
    }

    /*
    Creates a special mob spawner item which store an a nbt tag ("TYPE") which stores an EntityType. This tag is then
    used to determine what type of spawn
     */
    private ItemStack createSpawner(EntityType spawned, long expireStartTime) {
        // Create the spawner item stack and an ItemMeta object to edits its name/lore
        final ItemStack spawner = new ItemStack(Material.MOB_SPAWNER, 1);
        final ItemMeta meta = spawner.getItemMeta();

        // Calculate the expire time of this spawner
        final long expireTime = calculateExpireTime(expireStartTime);

        // Set the name/lore of the meta and set the meta to the itemstack
        meta.setDisplayName(Utils.color(config.getString("spawners.name-format")
                .replace("%type%", formatEntityName(spawned))));
        final List<String> lore = config.getStringList("spawners.lore-format")
                .stream().map(str -> str.replace("%time%", expireTime == -1 ? "N/A" : expireFormat.format(new Date(expireTime)))).collect(Collectors.toList());
        meta.setLore(Utils.colorList(lore));
        spawner.setItemMeta(meta);

        // Creare the NBTItem for the spawner, and set its type and expire time
        final NBTItem finalSpawner = new NBTItem(spawner);
        finalSpawner.setString(NBT_SPAWNER_TYPE, spawned.name());
        finalSpawner.setLong(NBT_SPAWNER_EXPIRE, expireTime);
        return finalSpawner.getItem();
    }

    private ItemStack createSpawner(EntityType spawned) {
        return createSpawner(spawned, System.currentTimeMillis());
    }

    private boolean playerHasSpawner(Player player, EntityType type) {
        final ItemStack targetSpawner = createSpawner(type);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(targetSpawner)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCustomSpawner(ItemStack item) {
        return item != null && new NBTItem(item).hasKey(NBT_SPAWNER_TYPE);
    }

    private void spawnMobOnSpawner(Player player, ItemStack playerItem, Block block, BlockFace face) {
        final EntityType type = Utils.getEntityTypeFromSpawnEgg(playerItem.getDurability());

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
        if (config.getBoolean("spawners.require-silk-touch")) {
            // Check to see if the player is holding an item with silk touch
            final ItemStack playerItem = player.getItemInHand();
            if (playerItem == null || !playerItem.containsEnchantment(Enchantment.SILK_TOUCH)) {
                lifetimeHelper(block.getLocation(), false);
                return;
            }

            // Check if the player has permission to mine spawners with silk touch
            if (!player.hasPermission(config.getString("spawners.permissions.mine-with-silk"))) {
                event.setCancelled(true);
                player.sendMessage(Utils.color(config.getString("spawners.messages.no-permission-to-mine")));
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
                (config.getBoolean("spawners.tax.enabled")) ? spawnerTax.getOrDefault(tempType, 0.0) : 0.0,
                Math.random() * 100
        );
        Bukkit.getPluginManager().callEvent(spawnerMineEvent);
        if (spawnerMineEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // Run % chance to actually drop the spawner from mining it
        if (spawnerMineEvent.getChanceToMine() > config.getDouble("spawners.mining-drop-chance")) {
            lifetimeHelper(block.getLocation(), false);
            player.sendMessage(Utils.color(config.getString("spawners.messages.failure-mined-spawner")));
            return;
        }
        event.setCancelled(true);

        // Check if spawner tax is enabled and the spawners entity type is taxed
        boolean withdraw = false;
        if (config.getBoolean("spawners.tax.enabled")) {
            // Check if the player has enough money
            final double balance = economy.getBalance(player);
            if (balance < spawnerMineEvent.getTax()) {
                player.sendMessage(Utils.color(config.getString("spawners.messages.not-enough-money")
                        .replace("%cost%", "" + spawnerMineEvent.getTax()).replace("%balance%", "" + balance)));
                return;
            } else {
                withdraw = true;
            }

        }

        // Create the spawner item then check to see whether to add the spawners directly to the players inventory, or drop naturally
        final EntityType type = spawnerMineEvent.getSpawnerType();
        final ItemStack spawnerItem = (spawnerTypes.contains(type) ? createSpawner(type) : createSpawner(EntityType.PIG));
        if (config.getBoolean("spawners.direclty-to-inventory")) {
            if (!playerHasSpawner(player, type) && player.getInventory().firstEmpty() == -1) {
                player.sendMessage(Utils.color(config.getString("spawners.messages.inventory-full")));
                return;
            }
            player.getInventory().addItem(spawnerItem);
        } else {

            block.getWorld().dropItem(block.getLocation(), spawnerItem);
        }

        if (withdraw) {
            economy.withdrawPlayer(player, spawnerMineEvent.getTax());
            player.sendMessage(Utils.color(config.getString("spawners.messages.has-enough-money").replace("%cost%", "" + spawnerMineEvent.getTax())));
        }

        // Send a confirmation message to the player that they just mined a spawner, and set the blocks type to air
        player.sendMessage(Utils.color(config.getString("spawners.messages.success-mined-spawner")
                .replace("%type%", formatEntityName(type))));
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
        if (config.getBoolean("spawners.lock") && !player.hasPermission(config.getString("spawners.permissions.spawner-command"))) {
            player.sendMessage(Utils.color(config.getString("spawners.messages.lock-enabled")));
            event.setCancelled(true);
            return;
        }

        // Check to see if the spawner has expired
        final NBTItem nbtItem = new NBTItem(player.getItemInHand());
        if (config.getBoolean("spawners.expire.enabled")) {
            // Check if the spawner has an expire time, and the spawner is expired
            final long expireTime = nbtItem.getLong(NBT_SPAWNER_EXPIRE);
            if (expireTime != -1 && expireTime < System.currentTimeMillis()) {
                player.sendMessage(Utils.color(config.getString("spawners.messages.expired-spawner")));
                event.setCancelled(true);
                return;
            // Check if the spawner does NOT have an expire time and no expire spawners are blocked
            } else if (expireTime == -1 && config.getBoolean("spawners.expire.block-no-expire-spawners")) {
                player.sendMessage(Utils.color(config.getString("spawners.messages.no-expire-spawner-blocked")));
                event.setCancelled(true);
                return;
            }
        }

        // Create the SpawnerPlaceEvent and call it. Make sure the event hasnt been cancelled before proceeding
        final CreatureSpawner spawner = (CreatureSpawner) block.getState();
        final EntityType tempType = EntityType.valueOf(nbtItem.hasKey(NBT_SPAWNER_TYPE) ? nbtItem.getString(NBT_SPAWNER_TYPE) : "PIG");
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
        player.sendMessage(Utils.color(config.getString("spawners.messages.placed-spawner")
                .replace("%type%", formatEntityName(spawnerPlaceEvent.getSpawnerType()))));

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
                        dropChances.getOrDefault(event.getEntityType(), 0.0)
                );
                Bukkit.getPluginManager().callEvent(spawnerExplodeEvent);

                // Run % chance to actually drop the spawner from explosion
                final double r = ThreadLocalRandom.current().nextDouble(0, 101);
                if (r > spawnerExplodeEvent.getChance()) {
                    continue;
                }

                // Grab the type of mob spawned by said spawner, and new spawner item that matches its type at the blocks location
                block.getWorld().dropItem(block.getLocation(), createSpawner(spawnerExplodeEvent.getSpawnerType(), explodeTime));

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
            if (!config.getBoolean("spawners.change.enabled")) {
                spawnMobOnSpawner(player, playerItem, block, event.getBlockFace());
                return;
            }

            // Check if the player has permission to change spawners with spawn eggs
            if (!player.hasPermission(config.getString("spawners.change.permission"))) {
                spawnMobOnSpawner(player, playerItem, block, event.getBlockFace());
                return;
            }

            // Fire the SpawnerChangeEvent and handle the rest of the event
            final CreatureSpawner spawner = (CreatureSpawner) block.getState();
            final SpawnerChangeEvent changeEvent = new SpawnerChangeEvent(
                    player,
                    spawner,
                    calculateLifetime(block),
                    Utils.getEntityTypeFromSpawnEgg(playerItem.getDurability())
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
        } else if (playerItem.getType() == Material.WATCH && !playerItem.hasItemMeta() && config.getBoolean("spawners.lifetime")) {
            Long time = spawnerLifetime.get(block.getLocation());
            if (time == null) {
                final long temp = System.currentTimeMillis();
                spawnerLifetime.put(block.getLocation(), temp);
                time = temp;
            }

            player.sendMessage(Utils.color(config.getString("spawners.messages.lifetime").replace("%time%", Utils.formatSecondsAsTime((int) ((System.currentTimeMillis() - time)/1000)))));
        }
    }

    /*
    Spawner Command
    /spawner - opens this menu
    /spawner reload - reloads the config.yml data
    /spawner list - shows all the types of available spawners
    /spawner give <player> <type> - gives the target player a spawner of type
    /spawner give <player> <type> <amount> - gives the target player amount of type spawner(s)
    /spawner all <type> - gives all online players type spawner
    /spawner all <type> <amount> - gives all online players amount of type spawner(s)
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check if the player has the spawner command permission. None of these commands should be used by normal players
        if (!sender.hasPermission(config.getString("spawners.permissions.spawner-command"))) {
            sender.sendMessage(Utils.color(config.getString("spawners.messages.command-no-perm")));
            return true;
        }

        // /spawner and /spawner help
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Utils.color(String.join("\n", config.getStringList("spawners.messages.command"))));
            return true;
        }

        // /spawner reload
        if (args[0].equalsIgnoreCase("reload")) {
            fileManager.reloadConfigurations();
            dataManager.reloadData(fileManager.getConfigYml());
            sender.sendMessage(Utils.color(config.getString("spawners.messages.reload")));
            return true;
        }

        // /spawner list
        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(Utils.color(config.getString("spawners.messages.list").replace("%types%", formatEntityTypes())));
            return true;
        }

        // /spawner give <player> <type> and /spawner give <player> <type> <amount>
        if (args[0].equalsIgnoreCase("give")) {
            try {
                // Grab the player, spawner type, and amount args (if the amount arg is not used default amount is 1)
                final Player targetPlayer = Bukkit.getPlayer(args[1]);
                final EntityType type = spawnerTypes.contains(EntityType.valueOf(args[2].toUpperCase()))
                        ? EntityType.valueOf(args[2].toUpperCase()) : EntityType.PIG;
                final int amount = (args.length == 4) ? Integer.parseInt(args[3]) : 1;

                // Create the spawner item, set its amount, and give it to the player
                final ItemStack spawnerItem = createSpawner(type);
                spawnerItem.setAmount(amount);
                Utils.safeItemGive(targetPlayer, spawnerItem);

                // Send the command sender and the player who received a spawner confirmation messages
                sender.sendMessage(Utils.color(config.getString("spawners.messages.give-spawner-sender")
                        .replace("%amount%", "" + amount).replace("%type%", formatEntityName(type))
                        .replace("%player%", targetPlayer.getName())));
                targetPlayer.sendMessage(Utils.color(config.getString("spawners.messages.give-spawner-receiver")
                        .replace("%amount%", "" + amount).replace("%type%", formatEntityName(type))));

                return true;
            } catch (Exception e) {
                sender.sendMessage(Utils.color(config.getString("spawners.messages.incorrect-usage")));
                e.printStackTrace();
                return true;
            }

        }

        // /spawner all <type> and /spawner all <type> <amount>
        if (args[0].equalsIgnoreCase("all")) {
            try {
                // Grab the spawner type and the amount
                final EntityType type = EntityType.valueOf(args[1].toUpperCase());
                final int amount = (args.length == 3) ? Integer.parseInt(args[2]) : 1;

                // Create the spawner item and set the amount
                final ItemStack spawnerItem = createSpawner(type);
                spawnerItem.setAmount(amount);

                // Give all the online players a spawner item and a message that they received a spawner
                final String receiverMessage = Utils.color(config.getString("spawners.messages.all-spawner-receiver")
                        .replace("%amount%", "" + amount).replace("%type%", formatEntityName(type)));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Utils.safeItemGive(player, spawnerItem);
                    player.sendMessage(receiverMessage);
                }

                // Send the command executor a message saying they gave all online players type spawners
                sender.sendMessage(Utils.color(config.getString("spawners.messages.all-spawner-sender")
                        .replace("%amount%", "" + amount).replace("%type%", formatEntityName(type))));

                return true;
            } catch (Exception e) {
                sender.sendMessage(Utils.color(config.getString("spawners.messages.incorrect-usage")));
                e.printStackTrace();
                return true;
            }

        }


        // If no valid /spawner sub command is found, send an incorrect usage message
        sender.sendMessage(Utils.color(config.getString("spawners.messages.incorrect-usage")));
        return true;
    }

}
