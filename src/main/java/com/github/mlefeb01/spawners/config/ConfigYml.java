package com.github.mlefeb01.spawners.config;

import com.github.mlefeb01.spawners.SpawnerPlugin;
import com.github.mlefeb01.spigotutils.api.config.AbstractConfig;
import com.github.mlefeb01.spigotutils.api.utils.TextUtils;
import lombok.Getter;
import org.bukkit.entity.EntityType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class ConfigYml extends AbstractConfig {

    public ConfigYml(SpawnerPlugin plugin) {
        super(plugin, "config.yml");
    }

    // Settings
    @Getter
    private boolean requireSilkTouch;
    @Getter
    private double miningDropChance;
    @Getter
    private boolean dropDirectlyToInventory;
    @Getter
    private boolean spawnerLock;
    @Getter
    private boolean spawnerChange;
    @Getter
    private String changePermission;
    @Getter
    private Map<EntityType, Double> spawnerDropChances;
    @Getter
    private boolean lifetimeEnabled;
    @Getter
    private boolean expireEnabled;
    @Getter
    private boolean blockNoExpireSpawners;
    @Getter
    private int expireTimeLimit;
    @Getter
    private boolean roundNearestHour;
    @Getter
    private SimpleDateFormat expireTimeFormat;
    @Getter
    private String spawnerItemName;
    @Getter
    private List<String> spawnerItemLore;

    // Tax
    @Getter
    private boolean spawnerTax;
    @Getter
    private Map<EntityType, Double> taxPrices;

    // Whitelist
    @Getter
    private Set<EntityType> typeWhitelist;

    // Permissions
    @Getter
    private String mineWithSilkPermission;
    @Getter
    private String bypassSpawnerLockPermission;
    @Getter
    private String spawnerCommandPermission;

    // Messages
    @Getter
    private String reloadMessage;
    @Getter
    private String successMinedSpawnerMessage;
    @Getter
    private String failureMinedSpawnerMessage;
    @Getter
    private String noPermissionToMineMessage;
    @Getter
    private String placedSpawnerMessage;
    @Getter
    private String expiredSpawnerMessage;
    @Getter
    private String noExpireSpanwerBlockedMessage;
    @Getter
    private String lockEnabledMessage;
    @Getter
    private String inventoryFullMessage;
    @Getter
    private String commandNoPermMessage;
    @Getter
    private String incorrectUsageMessage;
    @Getter
    private String giveSpawnerSenderMessage;
    @Getter
    private String giveSpawnerReceiverMessage;
    @Getter
    private String allSpawnerSenderMessage;
    @Getter
    private String allSpawnerReceiverMessage;
    @Getter
    private String notEnoughMoneyMessage;
    @Getter
    private String hasEnoughMoneyMessage;
    @Getter
    private String commandMessage;
    @Getter
    private String listMessage;
    @Getter
    private String lifetimeMessage;

    @Override
    protected void cache() {
        // Settings
        requireSilkTouch = config.getBoolean("settings.require-silk-touch");
        miningDropChance = config.getDouble("settings.mining-drop-chance");
        dropDirectlyToInventory = config.getBoolean("settings.directly-to-inventory");
        spawnerLock = config.getBoolean("settings.spawner-lock");
        spawnerChange = config.getBoolean("settings.spawner-change.enabled");
        changePermission = config.getString("settings.spawner-change.permission");
        spawnerDropChances = Collections.unmodifiableMap(new EnumMap<EntityType, Double>(config.getConfigurationSection("settings.explode-drop-chances").getKeys(false).stream().collect(Collectors.toMap(
                EntityType::valueOf,
                k -> config.getDouble("settings.explode-drop-chances." + k)
        ))));
        lifetimeEnabled = config.getBoolean("settings.lifetime");
        expireEnabled = config.getBoolean("settings.expire.enabled");
        blockNoExpireSpawners = config.getBoolean("settings.expire.block-no-expire-spawners");
        expireTimeLimit = config.getInt("settings.expire.time-limit");
        roundNearestHour = config.getBoolean("settings.expire.round-nearest-hour");
        expireTimeFormat = new SimpleDateFormat();
        expireTimeFormat.applyPattern(config.getString("settings.expire.time-placeholder-format"));
        expireTimeFormat.setTimeZone(TimeZone.getTimeZone("settings.expire.timezone"));
        spawnerItemName = TextUtils.color(config.getString("settings.name-format"));
        spawnerItemLore = Collections.unmodifiableList(TextUtils.colorList(config.getStringList("settings.lore-format")));

        // Tax
        spawnerTax = config.getBoolean("tax.enabled");
        taxPrices = Collections.unmodifiableMap(new EnumMap<EntityType, Double>(config.getConfigurationSection("tax.costs").getKeys(false).stream().collect(Collectors.toMap(
                EntityType::valueOf,
                k -> config.getDouble("tax.costs." + k)
        ))));

        // Whitelist
        typeWhitelist = Collections.unmodifiableSet(EnumSet.copyOf(config.getStringList("whitelist").stream().map(EntityType::valueOf).collect(Collectors.toList())));

        // Permissions
        mineWithSilkPermission = config.getString("permissions.mine-with-silk");
        bypassSpawnerLockPermission = config.getString("permissions.bypass-lock");
        spawnerCommandPermission = config.getString("permissions.spawner-command");

        // Messages
        reloadMessage = getMessage("messages.reload");
        successMinedSpawnerMessage = getMessage("messages.success-mined-spawner");
        failureMinedSpawnerMessage = getMessage("messages.failure-mined-spawner");
        noPermissionToMineMessage = getMessage("messages.no-permission-to-mine");
        placedSpawnerMessage = getMessage("messages.placed-spawner");
        expiredSpawnerMessage = getMessage("messages.expired-spawner");
        noExpireSpanwerBlockedMessage = getMessage("messages.no-expire-spawner-blocked");
        lockEnabledMessage = getMessage("messages.lock-enabled");
        inventoryFullMessage = getMessage("messages.inventory-full");
        commandNoPermMessage = getMessage("messages.command-no-perm");
        incorrectUsageMessage = getMessage("messages.incorrect-usage");
        giveSpawnerSenderMessage = getMessage("messages.give-spawner-sender");
        giveSpawnerReceiverMessage = getMessage("messages.give-spawner-receiver");
        allSpawnerSenderMessage = getMessage("messages.all-spawner-sender");
        allSpawnerReceiverMessage = getMessage("messages.all-spawner-receiver");
        notEnoughMoneyMessage = getMessage("messages.not-enough-money");
        hasEnoughMoneyMessage = getMessage("messages.has-enough-money");
        commandMessage = getMultiMessage("messages.command");
        listMessage = getMessage("messages.list");
        lifetimeMessage = getMessage("messages.lifetime");
    }

}
