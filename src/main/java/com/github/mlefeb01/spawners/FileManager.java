package com.github.mlefeb01.spawners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileManager {
    private static final String[] YML_FILES = {"config.yml"};
    private static final String[] JSON_FILES = {};
    private static final String[] DIRECTORIES = {};
    private final SpawnerPlugin plugin;
    private final Map<String, FileConfiguration> configFiles;

    public FileManager(SpawnerPlugin main) {
        this.plugin = main;
        this.configFiles = new HashMap<>();
    }

    // Initializes the plugins data folder, the actual data folder, and the yml/json files
    public void fileSetup() {
        // Creates the "Jackpot" folder in the /plugins/ directory
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        // Check to see if all directories have been created
        for (String directory : DIRECTORIES) {
            final Path dataFolder = Paths.get(plugin.getDataFolder().toPath().toString(), directory);
            if (!Files.exists(dataFolder)) {
                try {
                    Files.createDirectories(plugin.getDataFolder().toPath().resolve(directory));
                } catch (Exception e) {
                    e.printStackTrace();
                    plugin.getLogger().info("FAILED TO CREATE " + directory + "  folder");
                }
            }
        }


        // Check to see if the JSON files exist, if not create them
        for (String jsonFile : JSON_FILES) {
            final Path path = getJSONPath(jsonFile);
            if (!Files.exists(path)) {
                try {
                    Files.copy(getClass().getClassLoader().getResourceAsStream("data/" + jsonFile), path);
                } catch (IOException e) {
                    plugin.getLogger().warning("FAILED TO CREATE " + jsonFile);
                }
            }
        }

        // Check if YML files exist, and load them
        for (String ymlFile : YML_FILES) {
            final Path path = Paths.get(plugin.getDataFolder().toPath().toString(), ymlFile);
            if (!Files.exists(path)) {
                try {
                    Files.copy(getClass().getClassLoader().getResourceAsStream(ymlFile), path);
                } catch (Exception e) {
                    plugin.getLogger().info("FAILED TO CREATE " + ymlFile);
                }
            }

            configFiles.put(ymlFile, YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), ymlFile)));
        }

    }

    // Returns the path a JSON file named fileName would be located in this plugin
    public Path getJSONPath(String fileName) {
        return plugin.getDataFolder().toPath().resolve("data").resolve(fileName);
    }

    // Reloads YML FileConfigurations without breaking references
    public void reloadConfigurations() {
        for (String ymlFile : YML_FILES) {
            try {
                configFiles.get(ymlFile).load(plugin.getDataFolder().toPath().toAbsolutePath().resolve(ymlFile).toFile());
            } catch (Exception e) {
            }
        }
    }

    // Returns the config.yml FileConfiguration
    public FileConfiguration getConfigYml() {
        return configFiles.get("config.yml");
    }

}
