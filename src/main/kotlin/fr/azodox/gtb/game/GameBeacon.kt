package fr.azodox.gtb.game

import fr.azodox.gtb.animation.GameBeaconFliesBackAnimation
import fr.azodox.gtb.event.game.beacon.GameBeaconDepositedEvent
import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.CacheHelper
import fr.azodox.gtb.util.LocationSerialization
import fr.azodox.gtb.util.ProgressBarUtil
import fr.azodox.gtb.util.Schematic
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Flag
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.io.File
import java.util.*

private const val GAME_BEACON_PROTECTION_ENABLED_KEY = "game.beacon.protection.enabled"

private const val GAME_BEACON_PROTECTION_HEALTH_KEY = "game.beacon.protection.health"

private const val GAME_BEACON_PROTECTION_SCHEMATIC_FILE_KEY = "game.beacon.protection.schematic-file"

private const val GAME_BEACON_PROTECTION_SCHEMATIC_PASTE_LOCATION_KEY =
    "game.beacon.protection.schematic-paste-location"

private const val GAME_BEACON_PROTECTION_CRYSTALS_AMOUNT_KEY = "game.beacon.protection.end-crystals-amount"

private const val GAME_BEACON_PROTECTION_CRYSTALS_LOCATIONS_KEY = "game.beacon.protection.end-crystals-locations"

private const val GAME_BEACON_MAX_BASE_PLACEMENTS_KEY = "game.beacon.max-base-placements"

const val GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT = "beacon_pickedup"

private const val DAY_TIME: Long = 1000

private const val NIGHT_TIME: Long = 18000

class GameBeacon(
    val game: Game,
    private val defaultLocation: Location,
    var state: GameBeaconState,
    val defaultHealth: Double
) {

    private lateinit var itemStack: ItemStack
    internal lateinit var block: Block
    lateinit var slime: Slime
        private set
    lateinit var protection: GameBeaconProtection
        private set
    lateinit var owningTeam: GameTeam

    private var basePlacementCounter: Int = 0
    private var instabilityLevel: Double = 0.0
    private var locked: Boolean = false

    var health: Double = defaultHealth

    fun spawnAtDefaultLocation() {
        defaultLocation.world.time = DAY_TIME
        state = GameBeaconState.CENTER
        spawn(defaultLocation)
    }

    fun returnToDefaultLocation() {
        slime.remove()
        health = defaultHealth

        val display = block.location.world.spawn(block.location, BlockDisplay::class.java)
        display.block = block.blockData

        block.type = Material.AIR
        CacheHelper.put(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display", display)
        GameBeaconFliesBackAnimation(this, defaultLocation).runTaskTimer(game.plugin, 0, 1)
    }

    private fun spawn(location: Location) {
        val world = location.world ?: return
        val blockAt = world.getBlockAt(location)

        world.difficulty = Difficulty.EASY
        location.x = location.blockX + 0.5
        location.z = location.blockZ + 0.5
        slime = world.spawn(location, Slime::class.java)

        slime.isInvisible = true
        slime.setAI(false)
        slime.setWander(false)
        slime.isPersistent = true
        slime.removeWhenFarAway = false
        slime.isSilent = true
        slime.customName(Component.text("test"))
        slime.isCustomNameVisible = true
        slime.size = 3
        slime.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.setBaseValue(this.defaultHealth)
        slime.health = this.defaultHealth

        blockAt.type = Material.BEACON
        block = blockAt
    }

    fun triggerProtection() {
        if (state == GameBeaconState.PROTECTED) return
        val plugin = game.plugin
        this.protection = GameBeaconProtection(
            this,
            plugin.config.getDouble(
                GAME_BEACON_PROTECTION_HEALTH_KEY
            )
        )

        val schematicFile =
            File(plugin.dataFolder, "schematics/${plugin.config.getString(GAME_BEACON_PROTECTION_SCHEMATIC_FILE_KEY)}")
        schematicFile.mkdirs()

        val schematic = Schematic(plugin, schematicFile).loadSchematic()

        protection.enable(
            schematic,
            LocationSerialization.deserialize(
                plugin.config.getString(
                    GAME_BEACON_PROTECTION_SCHEMATIC_PASTE_LOCATION_KEY
                )
            ),
            plugin.config.getInt(GAME_BEACON_PROTECTION_CRYSTALS_AMOUNT_KEY),
            plugin.config.getStringList(GAME_BEACON_PROTECTION_CRYSTALS_LOCATIONS_KEY)
                .map { LocationSerialization.deserialize(it) }
        )
        this.state = GameBeaconState.PROTECTED
    }

    fun pickUp(player: Player) {
        val team = game.getPlayerTeam(player) ?: return
        val playerLocation = player.eyeLocation
        val location = playerLocation.subtract(playerLocation.direction)
        location.y = playerLocation.y + 1.0
        location.x += 0.5
        location.z += 0.5
        val world = location.world
        val display = world.spawn(location, BlockDisplay::class.java)
        display.block = block.blockData
        CacheHelper.put(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder", player.uniqueId)
        CacheHelper.put(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display", display)
        block.type = Material.AIR
        world.time = NIGHT_TIME
        game.getOnlinePlayers().forEach {
            it.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f)
        }
        state = GameBeaconState.PICKED_UP
        val checker = Bukkit.getScheduler()
            .runTaskTimer(game.plugin, GameBeaconDepositChecker(this, team.beaconDeposit), 0, 20)
        CacheHelper.put(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_checker", checker)
    }

    fun drop(player: Player, dropLocation: Location) {
        val holder = CacheHelper.get<UUID>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder") ?: return
        if (holder != player.uniqueId) return
        CacheHelper.get<BukkitTask>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_checker")?.cancel()
        CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_checker")
        CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder")
        val display = CacheHelper.get<BlockDisplay>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display") ?: return
        dropLocation.add(0.0, 3.0, 0.0)
        display.teleport(dropLocation)
        GameBeaconFliesBackAnimation(this, defaultLocation).runTaskTimer(game.plugin, 0, 1)
        state = GameBeaconState.DROPPED
    }

    fun deposit(player: Player) {
        game.getPlayerTeam(player)?.let { team ->
            this.owningTeam = team
            basePlacementCounter++
            CacheHelper.get<BukkitTask>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_checker")?.cancel()
            CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_checker")
            CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder")
            CacheHelper.get<BlockDisplay>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display")?.remove()
            CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display")
            Bukkit.getPluginManager().callEvent(GameBeaconDepositedEvent(this.game, this, player))

            val blockLocation = team.beaconDeposit.blockLocation
            if (basePlacementCounter >= game.plugin.config.getInt(GAME_BEACON_MAX_BASE_PLACEMENTS_KEY)) {
                this.locked = true
                blockLocation.world.getBlockAt(blockLocation).type = Material.BEACON
            }else
                this.spawn(blockLocation)
            blockLocation.world.time = DAY_TIME
            state = GameBeaconState.BASE
        }
    }
}

class GameBeaconProtection(
    private val beacon: GameBeacon,
    var health: Double,
    private val defaultHealth: Double = health,
    private val bossBars: MutableMap<UUID, BossBar> = mutableMapOf(),
    private val endCrystals: MutableList<UUID> = mutableListOf()
) {

    private val healthDisplays: MutableMap<UUID, TextDisplay> = mutableMapOf()
    private lateinit var protectionSchematic: Schematic
    private lateinit var protectionSchematicLocation: Location
    private var endCrystalDefaultHealth: Double = 0.0

    fun enable(
        schematic: Schematic,
        schematicLocation: Location,
        crystalAmount: Int,
        crystalLocations: List<Location>
    ) {
        val location = beacon.block.location
        val playersNearby = location.getNearbyEntitiesByType(Player::class.java, 3.0)

        playersNearby.forEach { player ->
            player.velocity = Vector(-2.0, 0.0, -2.0)
        }

        beacon.slime.remove()

        protectionSchematic = schematic
        protectionSchematicLocation = schematicLocation
        protectionSchematic.pasteSchematic(protectionSchematicLocation)

        beacon.block = location.world.getBlockAt(beacon.block.location)

        beacon.game.getOnlinePlayers().forEach { player ->
            val bossBar = BossBar.bossBar(
                language(player).message(GAME_BEACON_PROTECTION_ENABLED_KEY),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS,
                setOf(Flag.CREATE_WORLD_FOG, Flag.DARKEN_SCREEN)
            )
            bossBar.addViewer(player)
            this.bossBars[player.uniqueId] = bossBar
        }
        spawnCrystals(crystalAmount, crystalLocations)
    }

    private fun disable() {
        bossBars.forEach { (uuid, bar) ->
            Bukkit.getPlayer(uuid)?.let {
                bar.removeViewer(it)
            }
        }
        bossBars.clear()
        endCrystals.clear()
        healthDisplays.values.forEach(Entity::remove)
        healthDisplays.clear()
        protectionSchematic.undo(protectionSchematicLocation, listOf(Material.BEACON, Material.DIAMOND_BLOCK))
        beacon.state = GameBeaconState.VULNERABLE
    }

    private fun spawnCrystals(amount: Int, locations: List<Location>) {
        val healthPer: Double = this.health / amount.toDouble()
        endCrystalDefaultHealth = healthPer
        locations.forEach { location ->
            val world = location.world
            val crystal = world.spawn(location, EnderCrystal::class.java)
            crystal.isInvulnerable = false
            crystal.persistentDataContainer[NamespacedKey(beacon.game.plugin, "health"), PersistentDataType.DOUBLE] =
                healthPer
            crystal.isPersistent = true
            crystal.beamTarget = beacon.block.location
            endCrystals.add(crystal.uniqueId)

            val healthDisplay = world.spawn(location.add(0.0, 2.0, 0.0), TextDisplay::class.java)
            healthDisplay.text(
                Component.text(
                    ProgressBarUtil.getProgressBar(
                        healthPer,
                        healthPer,
                        40,
                        '|',
                        "§d",
                        "§7"
                    )
                )
            )
            healthDisplay.alignment = TextDisplay.TextAlignment.CENTER
            healthDisplay.billboard = Display.Billboard.CENTER
            healthDisplay.displayHeight = 10f
            healthDisplay.displayWidth = 10f
            healthDisplays[crystal.uniqueId] = healthDisplay
        }
    }

    fun updateCrystalDisplay(crystal: EnderCrystal) {
        val health =
            crystal.persistentDataContainer[NamespacedKey(beacon.game.plugin, "health"), PersistentDataType.DOUBLE]
        health?.let { hp ->
            healthDisplays[crystal.uniqueId]?.text(
                Component.text(
                    ProgressBarUtil.getProgressBar(
                        hp,
                        endCrystalDefaultHealth,
                        40,
                        '|',
                        "§d",
                        "§7"
                    )
                )
            )
        }
    }

    fun endCrystalDies(crystal: EnderCrystal) {
        healthDisplays[crystal.uniqueId]?.remove()
        endCrystals.remove(crystal.uniqueId)
    }

    fun damage(damage: Double) {
        this.health -= damage

        if (this.health <= 0) {
            this.disable()
        } else {
            updateBossBars()
        }
    }

    private fun updateBossBars() {
        bossBars.values.forEach { bar ->
            bar.progress((health / defaultHealth).toFloat())
        }
    }
}

enum class GameBeaconState {
    CENTER,
    PROTECTED,
    VULNERABLE,
    PICKED_UP,
    DROPPED,
    BASE
}