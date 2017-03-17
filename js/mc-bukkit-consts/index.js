module.exports = {}

function fillIn(pkg, types) {
  if (!(types instanceof Array))
    types = [types]

  for (typeIdx in types) {
    var type = types[typeIdx]
    try {
        module.exports[type] = Java.type(pkg + "." + type)
    } catch (e) {
    }
  }
}

fillIn("org.bukkit", [
  // interfaces with types
  "BanList",
  // classes
  "Bukkit", "Color", "FireworkEffect", "Location", "Note", "WorldCreator",
  // enumerations
  "Achievement", "Art", "ChatColor", "CoalType", "CropState", "Difficulty",
  "DyeColor", "Effect", "EntityEffect", "GameMode", "GrassSpecies", "Instrument",
  "Material", "NetherWartsState", "Particle", "PortalType", "Rotation",
  "SandstoneType", "SkullType", "Sound", "Statistic", "TreeSpecies", "TreeType",
  "WeatherType", "WorldType"])

fillIn("org.bukkit.attribute", ["AttributeModifier", "Attribute"])

fillIn("org.bukkit.block", ["Biome", "BlockFace"])

fillIn("org.bukkit.block.banner", ["Pattern", "PatternType"])

fillIn("org.bukkit.boss", ["BarColor", "BarFlag", "BarStyle"])

fillIn("org.bukkit.command", ["Command", "FormattedCommandAlias",
       "MultipleCommandAlias", "PluginCommand", "SimpleCommandMap",
       "CommandExecutor", "TabCompleter"])

fillIn("org.bukkit.configuration.file", ["FileConfiguration", "YamlConfiguration"])

fillIn("org.bukkit.configuration.serialization", "ConfigurationSerializable")

// when has anyone actually used conversations?

fillIn("org.bukkit.enchantments", ["Enchantment", "EnchantmentTarget"])

// why not include all entities? It's long, slow, and has no noticable benefit
// if a script has access to them. Most of the time, they are in reation to
// events or other API calls. Only entities with underlying enums are included
fillIn("org.bukkit.entity", ["EnderDragon", "EntityType", "Horse", "Ocelot", 
       "Rabbit", "Skeleton", "Villager"]) 
       
fillIn("org.bukkit.event", ["Listener", "Event", "HandlerList", "EventPriority"])

function be(action) {
  return "Block" + action + "Event"
}

fillIn("org.bukkit.event.block", [be("Break"), be("Burn"), be("CanBuild"),
       be("Damage"), be("Dispense"), be(""), be("Exp"), be("Explode"), be("Fade"),
       be("Form"), be("FromTo"), be("Grow"), be("Ignite"), be("MultiPlace"),
       be("Physics"), be("Piston"), be("PistonExtend"), be("PistonRetract"),
       be("Place"), be("Redstone"), be("Spread"), "CauldronLevelChangeEvent",
       "EntityBlockFormEvent", "LeavesDecayEvent", "NotePlayEvent",
       "SignChangeEvent", "Action"])

fillIn("org.bukkit.event.enchantment", ["EnchantItemEvent", 
       "PrepareItemEnchantEvent"])

function ee(action) {
  return "Entity" + action + "Event"
} 

fillIn("org.bukkit.event.entity", ["AreaEffectCloudApplyEvent", 
       "CreatureSpawnEvent", "CreeperPowerEvent", "EnderDragonChangePhaseEvent",
       ee("AirChange"), ee("BreakDoor"), ee("Breed"), ee("ChangeBlock"),
       ee("CombustByBlock"), ee("CombustByEntity"), ee("Combust"),
       ee("CreatePortal"), ee("DamageByBlock"), ee("DamageByEntity"),
       ee("Damage"), ee("Death"), ee(""), ee("Explode"), ee("Interact"),
       ee("PortalEnter"), ee("Portal"), ee("PortalExit"), ee("RegainHealth"),
       ee("ShootBow"), ee("Tame"), ee("Target"), ee("TargetLivingEntity"),
       ee("Teleport"), ee("ToggleGlide"), ee("Unleash"), "ExpBottleEvent",
       "ExplosionPrimeEvent", "FireworkExplodeEvent", "FoodLevelChangeEvent", 
       "HorseJumpEvent", "ItemDespawnEvent", "ItemMergeEvent", "ItemSpawnEvent", 
       "LingeringPotionSplashEvent", "PigZapEvent", "PlayerDeathEvent",
       "PlayerLeashEntityEvent", "PotionSplashEvent", "ProjectileHitEvent", 
       "ProjectileLaunchEvent", "SheepDyeWoolEvent", "SheepRegrowWoolEvent",
       "SlimeSplitEvent", "VillagerAcquireTradeEvent",
       "VillagerReplenishTradeEvent"])

fillIn("org.bukkit.event.hanging", ["HangingBreakByEntityEvent",
       "HangingBreakEvent", "HangingEvent", "HangingPlaceEvent"])

function ie(action) {
  return "Inventory" + action + "Event"
}

fillIn("org.bukkit.event.inventory", ["BrewEvent", "CraftItemEvent",
       "FurnaceBurnEvent", "FurnaceExtractEvent", "FurnaceSmeltEvent",
       ie("Click"), ie("Close"), ie("Creative"), ie("Drag"), ie(""),
       ie("Interact"), ie("MoveItem"), ie("Open"), ie("PickupItem"),
       "PrepareAnvilEvent", "PrepareItemCraftEvent",
       // enums
       "ClickType", "DragType", "InventoryAction", "InventoryType"])

function pe(action) {
  return "Player" + action + "Event"
}
fillIn("org.bukkit.event.player", ["AsyncPlayerChatEvent",
       "AsyncPlayerPreLoginEvent", pe("AchievementAwarded"), pe("Animation"),
       pe("ArmorStandManipulate"), pe("BedEnter"), pe("BedLeave"),
       pe("BucketEmpty"), pe("Bucket"), pe("BucketFill"), pe("ChangedMainHand"),
       pe("ChangedWorld"), pe("Channel"), pe("Chat"), pe("ChatTabComplete"), 
       pe("CommandPreprocess"), pe("DropItem"), pe("EditBook"), pe("EggThrow"),
       pe(""), pe("ExpChange"), pe("Fish"), pe("GameModeChange"),
       pe("InteractAtEntity"), pe("InteractEntity"), pe("Interact"),
       pe("Inventory"), pe("ItemBreak"), pe("ItemConsume"), pe("ItemHeld"),
       pe("Join"), pe("Kick"), pe("LevelChange"), pe("Login"), pe("Move"),
       pe("PickupArrow"), pe("PickupItem"), pe("Portal"), pe("PreLogin"),
       pe("Quit"), pe("RegisterChannel"), pe("ResourcePackStatus"),
       pe("Respawn"), pe("ShearEntity"), pe("StatisticIncrement"),
       pe("SwapHandItems"), pe("Teleport"), pe("ToggleFlight"),
       pe("ToggleSneak"), pe("ToggleSprint"), pe("UnleashEntity"),
       pe("UnregisterChannel"), pe("Velocity")])

fillIn("org.bukkit.event.server", ["MapInitializeEvent", "PluginDisableEvent",
       "PluginEnableEvent", "PluginEvent", "RemoteServerCommandEvent", 
       "ServerCommandEvent", "ServerEvent", "ServerListPingEvent",
       "ServiceEvent", "ServiceRegisterEvent", "ServiceUnregisterEvent", 
       "TabCompleteEvent"])

function ve(action) {
  return "Vehicle" + action + "Event"
}

fillIn("org.bukkit.event.vehicle", [ve("BlockCollision"), ve("Collision"),
       ve("Create"), ve("Damage"), ve("Destroy"), ve("Enter"),
       ve("EntityCollision"), ve(""), ve("Exit"), ve("Move"), ve("Update")])

fillIn("org.bukkit.event.weather", ["LightningStrikeEvent", "ThunderChangeEvent",
       "WeatherChangeEvent", "WeatherEvent"])

fillIn("org.bukkit.event.world", ["ChunkEvent", "ChunkLoadEvent",
       "ChunkPopulateEvent", "ChunkUnloadEvent", "PortalCreateEvent",
       "SpawnChangeEvent", "StructureGrowEvent", "WorldEvent", "WorldInitEvent", 
       "WorldLoadEvent", "WorldSaveEvent", "WorldUnloadEvent"])

fillIn("org.bukkit.inventory", ["ItemStack", "InventoryView", "EquipmentSlot",
       "ItemFlag", "MainHand"])

fillIn("org.bukkit.inventory.meta", "BookMeta")

fillIn("org.bukkit.map", ["MapCursor", "MapCursorCollection", "MapFont",
       "MapPalette", "MapRenderer", "MinecraftFont"])

fillIn("org.bukkit.material", ["MaterialData", "CocoaPlant"])

fillIn("org.bukkit.material.types", "MushroomBlockTexture")

fillIn("org.bukkit.metadata", ["FixedMetadataValue", "LazyMetadataValue"])

fillIn("org.bukkit.permissions", ["PermissibleBase", "Permission", 
       "PermissionAttachment", "PermissionAttachmentInfo", "PermissionDefault"])

fillIn("org.bukkit.plugin", ["EventExecutor", "Plugin", "SimplePluginManager"])

fillIn("org.bukkit.plugin.messaging", ["Messenger", "PluginMessageListener",
       "StandardMessenger"])

fillIn("org.bukkit.potion", ["Potion", "PotionData", "PotionEffect",
       "PotionEffectType", "PotionType"])

fillIn("org.bukkit.scheduler", "BukkitRunnable")

fillIn("org.bukkit.scoreboard", ["Criterias", "DisplaySlot", "NameTagVisibility",
       "Team"])

fillIn("org.bukkit.util", ["BlockIterator", "BlockVector", "ChatPaginator",
       "EulerAngle", "FileUtil", "NumberConversions", "StringUtil", "Vector"])
