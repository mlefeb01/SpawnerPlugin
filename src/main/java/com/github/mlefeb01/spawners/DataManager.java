package com.github.mlefeb01.spawners;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataManager {
    private final SpawnerPlugin plugin;
    private final FileManager fileManager;
    private final Set<EntityType> spawnerTypes;
    private final Map<EntityType, Double> spawnerTax;
    private final Map<EntityType, Double> dropChances;
    private final SimpleDateFormat expireFormat;
    private final Map<Location, Long> spawnerLifetime;
    private Economy economy;

    public DataManager(SpawnerPlugin plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.spawnerTypes = new LinkedHashSet<>();
        this.spawnerTax = new HashMap<>();
        this.dropChances = new HashMap<>();
        this.expireFormat = new SimpleDateFormat();
        this.spawnerLifetime = new HashMap<>();
    }

    // Initializes one time load data (e.g. - from a JSON fle) and loads config data
    public void initializeData(FileConfiguration configYml, Gson gson) {
        economy = loadEconomy();
        spawnerLifetime.putAll(fileManager.loadMap(gson, fileManager.getJSONPath("spawners.json"), new TypeToken<HashMap<Location, Long>>(){}));
        reloadData(configYml);
    }

    // Only reloads config data
    public void reloadData(FileConfiguration configYml) {
        loadSpawnerTypes(configYml);
        loadSpawnerTax(configYml);
        loadSpawnerDropChances(configYml);
        loadExpireFormat(configYml);
    }

    // Loads the Vault Economy
    public Economy loadEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("NO ECONOMY PLUGIN FOUND - SPAWNER PLUGIN WILL NOT WORK FULLY");
            return null;
        }

        final RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("NO ECONOMY PLUGIN FOUND - SPAWNER PLUGIN WILL NOT WORK FULLY");
            return null;
        }

        return rsp.getProvider();
    }

    // Loads the EntityTypes that are allowed to be spawners
    private void loadSpawnerTypes(FileConfiguration configYml) {
        spawnerTypes.clear();

        configYml.getStringList("spawners.whitelist").forEach(spawner -> spawnerTypes.add(EntityType.valueOf(spawner)));
    }

    // Loads the amounts of money each spawner type requires to mine (if enabled)
    public void loadSpawnerTax(FileConfiguration configYml) {
        spawnerTax.clear();

        configYml.getConfigurationSection("spawners.tax.costs").getKeys(false).forEach(mob ->
                spawnerTax.put(EntityType.valueOf(mob.toUpperCase()), configYml.getDouble("spawners.tax.costs." + mob)));
    }

    // Loads the chances for different exploding entity types to drop spawners
    public void loadSpawnerDropChances(FileConfiguration configYml) {
        dropChances.clear();

        configYml.getConfigurationSection("spawners.explode-drop-chance").getKeys(false).forEach(entity ->
                dropChances.put(EntityType.valueOf(entity), configYml.getDouble("spawners.explode-drop-chance." + entity)));
    }

    // Loads the format in which the SimpleDateFormat should display the spawner items expire time
    public void loadExpireFormat(FileConfiguration configYml) {
        final String timezone = configYml.getString("spawners.expire.timezone");
        if (!timezone.isEmpty()) {
            expireFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        expireFormat.applyPattern(configYml.getString("spawners.expire.time-placeholder-format"));
    }

    public Economy getEconomy() {
        return economy;
    }

    public Set<EntityType> getSpawnerTypes() {
        return spawnerTypes;
    }

    public Map<EntityType, Double> getSpawnerTax() {
        return spawnerTax;
    }

    public Map<EntityType, Double> getDropChances() {
        return dropChances;
    }

    public SimpleDateFormat getExpireFormat() {
        return expireFormat;
    }

    public Map<Location, Long> getSpawnerLifetime() {
        return this.spawnerLifetime;
    }

}
