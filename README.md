# Spawner Plugin
An all in one Minecraft spawner plugin

## Motivation
The goal of creating this plugin was to provide a lightweight and feature/API rich plugin focused on spawners

## Features
* Mine Spawners with or without Silk Touch 
* Spawners drop from explosions
* Toggle spawner changing with mob eggs
* Spawner Tax (Money cost when mining different spawner types)
* Spawner Expire (Requires spawner items to be placed within a configurable time frame)
* Spawner Whitelist (Choose what entity types can be spawners)
* Spawner Lock (Toggle spawner placement)
* Spawner Lifetime (The amount of time in milliseconds that a spawner has been placed for)
* View spawner lifetime by right clicking a spawner with a watch 

## Dependencies
The only required dependency for this plugin is Vault, which can be
downloaded at https://dev.bukkit.org/projects/vault

## Custom Spigot Events
This plugin adds 4 new events...
* SpawnerExplodeEvent - Fired when a spawner is destroyed via explosion
* SpawnerMineEvent - Fired when a spawner is broken by a player
* SpawnerPlaceEvent - Fired when a player places a mob spawner
* SpawnerChangeEvent - Fired when a player changes a mob spawner with a mob egg

## SpawnerAPI
To use the API create a reference to the SpawnerAPI singleton 
~~~
SpawnerPlugin.getSpawnerAPI()
~~~
After creating a reference, you can use the following methods to access a spawners lifetime.
These methods will return the time the spawner has been placed for in milliseconds or -1 if the feature is disabled or 
the block is not a spawner
~~~
public long getSpawnerLifetime(Location location)
public long getSpawnerLifetime(Block block)
~~~
The API contains other methods such as 
~~~
public ItemStack createSpawner(EntityType spawned)
public ItemStack createSpawner(EntityType spawned, long expireStartTime)
public boolean isCustomSpawner(ItemStack item)
~~~