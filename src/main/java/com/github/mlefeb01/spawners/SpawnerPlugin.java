package com.github.mlefeb01.spawners;

import com.github.mlefeb01.spawners.command.SpawnerCommand;
import com.github.mlefeb01.spawners.config.ConfigYml;
import com.github.mlefeb01.spawners.listener.SpawnerListener;
import com.github.mlefeb01.spigotutils.api.adapters.LocationAdapter;
import com.github.mlefeb01.spigotutils.api.utils.FileUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class SpawnerPlugin extends JavaPlugin {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationAdapter()).enableComplexMapKeySerialization().setPrettyPrinting().create();
    private final ConfigYml configYml = new ConfigYml(this);
    private final Map<Location, Long> spawnerLifetime = new HashMap<>();
    private static SpawnerAPI spawnerAPI;

    @Override
    public void onEnable() {
        configYml.load();

        FileUtils.createFile(this, getDataFolder().toPath(), "spawners.json");
        spawnerLifetime.putAll(FileUtils.loadMap(gson, getDataFolder().toPath().resolve("spawners.json"), new TypeToken<HashMap<Location, Long>>(){}));

        final Economy economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        getServer().getPluginManager().registerEvents(new SpawnerListener(configYml, spawnerLifetime, economy), this);

        getCommand("spawner").setExecutor(new SpawnerCommand(configYml));

        spawnerAPI = new SpawnerAPI(configYml, spawnerLifetime);

        getLogger().info("SpawnerPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        FileUtils.saveMap(gson, getDataFolder().toPath().resolve("spawners.json"), spawnerLifetime);

        getLogger().info("SpawnerPlugin has been disabled!");
    }

    public static SpawnerAPI getSpawnerAPI() {
        return spawnerAPI;
    }

}
