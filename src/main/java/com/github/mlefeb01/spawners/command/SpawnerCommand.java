package com.github.mlefeb01.spawners.command;

import com.github.mlefeb01.spawners.SpawnerPlugin;
import com.github.mlefeb01.spawners.config.ConfigYml;
import com.github.mlefeb01.spigotutils.api.utils.PlayerUtils;
import com.github.mlefeb01.spigotutils.api.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public final class SpawnerCommand implements CommandExecutor {
    private final ConfigYml configYml;

    public SpawnerCommand(ConfigYml configYml) {
        this.configYml = configYml;
    }

    /*
    Spawner Command
    <> - required parameter
    [] - optional parameter
    /spawner - opens this menu
    /spawner reload - reloads the config.yml data
    /spawner list - shows all the types of available spawners
    /spawner give <player> <type> [amout] - gives the target player a spawner of type
    /spawner all <type> [amount] - gives all online players type spawner
    */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(configYml.getSpawnerCommandPermission())) {
            sender.sendMessage(configYml.getCommandNoPermMessage());
            return true;
        }

        // /spawner and /spawner help
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(configYml.getCommandMessage());

        // /spawner reload
        } else if (args[0].equalsIgnoreCase("reload")) {
            configYml.load();
            sender.sendMessage(configYml.getReloadMessage());

        // /spawner list
        } else if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(configYml.getListMessage().replace("%types%", configYml.getTypeWhitelist().stream().map(TextUtils::formatEnumAsString).collect(Collectors.joining(", "))));

        // /spawner give <player> <type> and /spawner give <player> <type> <amount>
        } else if (args[0].equalsIgnoreCase("give")) {
            try {
                final Player targetPlayer = Bukkit.getPlayer(args[1]);
                final EntityType type = configYml.getTypeWhitelist().contains(EntityType.valueOf(args[2].toUpperCase())) ? EntityType.valueOf(args[2].toUpperCase()) : EntityType.PIG;
                final int amount = (args.length == 4) ? Integer.parseInt(args[3]) : 1;

                final ItemStack spawnerItem = SpawnerPlugin.getSpawnerAPI().createSpawner(type);
                spawnerItem.setAmount(amount);
                PlayerUtils.safeItemGive(targetPlayer, spawnerItem);

                final String formattedEntity = TextUtils.formatEnumAsString(type);
                final String formattedAmount = String.format("%,d", amount);
                sender.sendMessage(configYml.getGiveSpawnerSenderMessage().replace("%amount%", formattedAmount).replace("%type%", formattedEntity).replace("%player%", targetPlayer.getName()));
                targetPlayer.sendMessage(configYml.getGiveSpawnerReceiverMessage().replace("%amount%", formattedAmount).replace("%type%", formattedEntity));

            } catch (Exception e) {
                sender.sendMessage(configYml.getIncorrectUsageMessage());
                e.printStackTrace();
            }

        // /spawner all <type> and /spawner all <type> <amount>
        } else if (args[0].equalsIgnoreCase("all")) {
            try {
                final EntityType type = EntityType.valueOf(args[1].toUpperCase());
                final int amount = (args.length == 3) ? Integer.parseInt(args[2]) : 1;

                final ItemStack spawnerItem = SpawnerPlugin.getSpawnerAPI().createSpawner(type);
                spawnerItem.setAmount(amount);

                final String formattedEntity = TextUtils.formatEnumAsString(type);
                final String formattedAmount = String.format("%,d", amount);

                final String receiverMessage = configYml.getAllSpawnerReceiverMessage().replace("%amount%", formattedAmount).replace("%type%", formattedEntity);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerUtils.safeItemGive(player, spawnerItem);
                    player.sendMessage(receiverMessage);
                }
                sender.sendMessage(configYml.getAllSpawnerSenderMessage().replace("%amount%", formattedAmount).replace("%type%", formattedEntity));

            } catch (Exception e) {
                sender.sendMessage(configYml.getIncorrectUsageMessage());
                e.printStackTrace();
            }

        } else {
            sender.sendMessage(configYml.getIncorrectUsageMessage());
        }

        return true;
    }

}
