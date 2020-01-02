package com.github.mlefeb01.spawners;

import com.github.mlefeb01.spawners.handlers.SpawnerHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Minectaft Plugin that allows for mining spawners, exploding spawners, spawner tax, spawner expire, and more
 * @author Matt Lefebvre (github.com/mlefeb01)
 */
public class SpawnerPlugin extends JavaPlugin {
    private final FileManager fileManager = new FileManager(this);
    private final DataManager dataManager = new DataManager(this);

    @Override
    public void onEnable() {
        // Initialize plugin directory/files
        fileManager.fileSetup();

        // Loads important config data
        dataManager.initializeData(fileManager.getConfigYml());

        // Create Listener/Command classes
        final SpawnerHandler spawnerHandler = new SpawnerHandler(
                fileManager,
                dataManager,
                dataManager.getSpawnerTypes(),
                fileManager.getConfigYml(),
                dataManager.getEconomy(),
                dataManager.getSpawnerTax(),
                dataManager.getDropChances(),
                dataManager.getExpireFormat()
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
    }

}
