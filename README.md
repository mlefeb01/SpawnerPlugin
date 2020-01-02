# Spawner Plugin
The all in one minecraft spawner plugin consisting of the following features...
* Mine Spawners with or without Silk Touch 
* Spawners drop from explosions
* 6 spawner commands
* Spawner Tax (Money cost when mining different spawner types)
* Spawner Expire (Requires spawner items to be placed within a configurable time frame)
* Spawner Whitelist (Choose what entity types can be spawners)
* Spawner Lock (Toggle spawner placement)

## Dependencies
The only required dependency for this plugin is Vault, which can be
downloaded at https://dev.bukkit.org/projects/vault

## Custom Spigot Events
This plugin features 3 custom spigot events
* SpawnerExplodeEvent - Fired when a spawner is destroye via explosion
* SpawnerMineEvent - Fired when a spawner is broken by a player
* SpawnerPlaceEvent - Fired when a player places a mob spawner

In order to use these events in your plugin, you need to add this plugin as a dependency. 
After doing that, you will be able to listen to these events in your own plugin!
