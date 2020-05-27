package com.github.mlefeb01.spawners;

import com.github.mlefeb01.spawners.handlers.SpawnerHandler;
import com.github.mlefeb01.spawners.utils.LocationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Minectaft Plugin that allows for mining spawners, exploding spawners, spawner tax, spawner expire, and more
 * @author Matt Lefebvre (github.com/mlefeb01)
 */
public class SpawnerPlugin extends JavaPlugin {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();
    private final FileManager fileManager = new FileManager(this);
    private final DataManager dataManager = new DataManager(this, fileManager);

    @Override
    public void onEnable() {
        // Initialize plugin directory/files
        fileManager.fileSetup();

        // Loads important config data
        dataManager.initializeData(fileManager.getConfigYml(), gson);

        // Create Listener/Command classes
        final SpawnerHandler spawnerHandler = new SpawnerHandler(
                fileManager,
                dataManager,
                dataManager.getSpawnerTypes(),
                fileManager.getConfigYml(),
                dataManager.getEconomy(),
                dataManager.getSpawnerTax(),
                dataManager.getDropChances(),
                dataManager.getExpireFormat(),
                dataManager.getSpawnerLifetime()
        );

        // Register Listeners
        getServer().getPluginManager().registerEvents(spawnerHandler, this);

        // Register Commands
        getCommand("spawner").setExecutor(spawnerHandler);

        // Log to console that the spawner plugin has enabled!
        getLogger().info("SpawnerPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Log to console the spawner plugin has been disabled
        getLogger().info("SpawnerPlugin has been disabled!");
        fileManager.saveMap(gson, fileManager.getJSONPath("spawners.json"), dataManager.getSpawnerLifetime());
    }

}
