package fr.azodox.gtb.game

import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.LocationSerialization
import fr.azodox.gtb.util.Schematic
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Flag
import net.kyori.adventure.text.Component
import org.bukkit.Difficulty
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.io.File

private const val GAME_BEACON_PROTECTION_ENABLED_KEY = "game.beacon.protection.enabled"

private const val GAME_BEACON_PROTECTION_SCHEMATIC_FILE_KEY = "game.beacon.protection.schematic-file"

private const val GAME_BEACON_PROTECTION_SCHEMATIC_PASTE_LOCATION_KEY = "game.beacon.protection.schematic-paste-location"

class GameBeacon(
    val plugin: Plugin,
    private val defaultLocation: Location,
    var state: GameBeaconState,
    val defaultHealth: Double
) {
    private val bossBars = mutableListOf<BossBar>()

    private lateinit var itemStack: ItemStack
    private lateinit var block: Block
    lateinit var slime: Slime
        private set
    private lateinit var owningTeam: GameTeam

    private var basePlacementCounter: Int = 0
    private var instabilityLevel: Double = 0.0
    private var locked: Boolean = false

    var health: Double = defaultHealth

    fun spawnAtDefaultLocation() {
        spawn(defaultLocation)
    }

    private fun spawn(location: Location) {
        val world = location.world ?: return
        val blockAt = world.getBlockAt(location)

        world.difficulty = Difficulty.EASY
        slime = world.spawn(location.add(0.5, 0.01, 0.5), Slime::class.java)

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
        this.state = GameBeaconState.PROTECTED

        val location = block.location
        val playersNearby = location.getNearbyEntitiesByType(Player::class.java, 3.0)

        playersNearby.forEach { player ->
            player.velocity = Vector(-2.0, 0.0, -2.0)
        }

        val schematicFile = File(plugin.dataFolder, "schematics/${plugin.config.getString(GAME_BEACON_PROTECTION_SCHEMATIC_FILE_KEY)}")
        schematicFile.mkdirs()

        val schematic = Schematic(plugin, schematicFile)
        schematic.loadSchematic()

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            schematic.pasteSchematic(
                LocationSerialization.deserialize(
                plugin.config.getString(
                    GAME_BEACON_PROTECTION_SCHEMATIC_PASTE_LOCATION_KEY
                )
            ))
        }, 10L)

        location.world.time = 18000

        plugin.server.onlinePlayers.forEach { player ->
            val bossBar = BossBar.bossBar(
                language(player).message(GAME_BEACON_PROTECTION_ENABLED_KEY),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS,
                setOf(Flag.CREATE_WORLD_FOG, Flag.DARKEN_SCREEN)
            )
            bossBar.addViewer(player)
            bossBars.add(bossBar)
        }
    }
}

enum class GameBeaconState {
    CENTER,
    PROTECTED,
    BASE
}