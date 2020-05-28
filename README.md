# Spawner Plugin
An all in one Minecraft spawner plugin consisting of the following features...
* Mine Spawners with or without Silk Touch 
* Spawners drop from explosions
* 6 spawner commands
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
This plugin adds 4 new events
* SpawnerExplodeEvent - Fired when a spawner is destroyed via explosion
* SpawnerMineEvent - Fired when a spawner is broken by a player
* SpawnerPlaceEvent - Fired when a player places a mob spawner
* SpawnerChangeEvent - Fired when a player changes a mob spawner with a mob egg

In order to use these events in your plugin, you need to add this plugin as a dependency. 
After doing that, you will be able to listen to these events in your own plugin!

## SpawnerAPI
To use the API create a reference to the SpawnerAPI singleton 
~~~
SpawnerPlugin.getSpawnerAPI()
~~~
After creating a reference, you can use the following methods to access a spawners lifetime.
These methods will return the time the spawner has been placed for in milliseconds or -1 if the feature is disabled or 
the block is not a spawner. 
~~~
public long getSpawnerLifetime(Location location)
public long getSpawnerLifetime(Block block)
~~~