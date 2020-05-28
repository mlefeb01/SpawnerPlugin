package com.github.mlefeb01.spawners;

import com.github.mlefeb01.spawners.handlers.SpawnerHandler;
import com.github.mlefeb01.spawners.utils.LocationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Minecraft Plugin that allows for mining spawners, exploding spawners, spawner tax, spawner expire, spawner lifetime, and more
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
    private static SpawnerAPI spawnerAPI;

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

        // API
        spawnerAPI = new SpawnerAPI(fileManager.getConfigYml(), dataManager.getSpawnerLifetime());
    }

    @Override
    public void onDisable() {
        // Log to console the spawner plugin has been disabled
        getLogger().info("SpawnerPlugin has been disabled!");
        fileManager.saveMap(gson, fileManager.getJSONPath("spawners.json"), dataManager.getSpawnerLifetime());
    }

    public static SpawnerAPI getSpawnerAPI() {
        return spawnerAPI;
    }

    public class SpawnerAPI {
        private final FileConfiguration config;
        private final Map<Location, Long> spawnerLifetime;

        private SpawnerAPI(FileConfiguration config, Map<Location, Long> spawnerLifetime) {
            this.config = config;
            this.spawnerLifetime = spawnerLifetime;
        }

        public long getSpawnerLifetime(Location location) {
            return getSpawnerLifetime(location.getBlock());
        }

        public long getSpawnerLifetime(Block block) {
            // Check if the lifetime feature is enabled
            if (!config.getBoolean("spawners.lifetime")) {
                return -1;
            }

            // Check for blocks that are not a spawner
            final Long time = spawnerLifetime.get(block.getLocation());
            if (block.getType() != Material.MOB_SPAWNER) {
                // Remove the location from the cache if for some reason the block is no longer a spawner (e.g. - worldedit)
                if (time != null) {
                    spawnerLifetime.remove(block.getLocation());
                }
                return -1;
            }

            // By now it is known that the block is a spawner, so if it does not have a lifetime cached give it one (e.g. - worldedit)
            if (time == null) {
                spawnerLifetime.put(block.getLocation(), System.currentTimeMillis());
                return 0;
            } else {
                return System.currentTimeMillis() - time;
            }
        }
    }

}
