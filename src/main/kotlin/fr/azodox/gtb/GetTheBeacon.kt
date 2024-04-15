package fr.azodox.gtb

import co.aikar.commands.PaperCommandManager
import fr.azodox.gtb.commands.LanguageCommand
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.team.GameBeaconDeposit
import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.lang.LanguageCore
import fr.azodox.gtb.listener.PlayerJoinListener
import fr.azodox.gtb.listener.PlayerQuitListener
import fr.azodox.gtb.listener.block.BlockBreakListener
import fr.azodox.gtb.listener.entity.EndCrystalTakesDamageListener
import fr.azodox.gtb.listener.entity.SlimeTakesDamageListener
import fr.azodox.gtb.listener.game.beacon.GameBeaconDepositedListener
import fr.azodox.gtb.listener.game.beacon.GameBeaconPickUpListener
import fr.azodox.gtb.listener.game.beacon.GameBeaconTakesDamageListener
import fr.azodox.gtb.listener.game.player.*
import fr.azodox.gtb.listener.game.player.environment.GamePlayerBreakBlockListener
import fr.azodox.gtb.listener.game.player.environment.GamePlayerDropsItemListener
import fr.azodox.gtb.listener.game.player.environment.GamePlayerPickupItemListener
import fr.azodox.gtb.listener.game.player.environment.GamePlayerPlaceBlockListener
import fr.azodox.gtb.listener.game.player.state.GamePlayerFoodLevelChangeListener
import fr.azodox.gtb.listener.game.player.state.GamePlayerTakesDamageListener
import fr.azodox.gtb.listener.inventory.PlayerInteractionListener
import fr.azodox.gtb.listener.state.GameStartsListener
import fr.azodox.gtb.listener.state.GameStateChangeListener
import fr.azodox.gtb.util.FileUtil
import fr.azodox.gtb.util.LocationSerialization
import me.devnatan.inventoryframework.ViewFrame
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Logger

class GetTheBeacon : JavaPlugin() {

    companion object {
        @JvmField
        val LOGGER: Logger = Logger.getLogger("GetTheBeacon")
    }

    val languageCore = LanguageCore(this)
    lateinit var game: Game
    lateinit var viewFrame: ViewFrame

    override fun onEnable() {
        saveDefaultConfig()
        this::class.java.getResource("/teams.yml")?.let { FileUtil.copyFilesFromJar(dataFolder, it.toURI()) }

        languageCore.init()
        game = Game(this, minPlayers = config.getInt("game.min-players"))
        viewFrame = ViewFrame.create(this)

        loadTeams()

        addEvents(
            GamePlayerBreakBlockListener(game),
            GamePlayerDropsItemListener(game),
            GamePlayerInitializationListener(this),
            GamePlayerJoinsTeamListener(game),
            GamePlayerLeavesTeamListener(),
            GamePlayerPlaceBlockListener(game),
            GamePlayerRemovedListener(),
            GamePlayerTakesDamageListener(game),
            GamePlayerFoodLevelChangeListener(game),
            GamePlayerGenericLobbyEventListener(),
            GamePlayerPickupItemListener(game),
            GameStateChangeListener(),
            GameStartsListener(),
            PlayerJoinListener(this),
            PlayerQuitListener(this),
            PlayerInteractionListener(game, this),
            SlimeTakesDamageListener(game),
            GameBeaconTakesDamageListener(),
            EndCrystalTakesDamageListener(this, game),
            GameBeaconPickUpListener(game),
            GamePlayerMovesListener(game),
            GamePlayerDiesListener(game),
            GameBeaconDepositedListener(),
            BlockBreakListener(game)
        )

        val manager = PaperCommandManager(this)
        manager.commandCompletions.registerAsyncCompletion("locales") {
            LanguageCore.languages.map { it.key }
        }
        manager.registerCommand(LanguageCommand(languageCore))

        server.worlds.forEach { world ->
            world.time = 0
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        }

        LOGGER.info("Enabled")
    }

    private fun loadTeams() {
        val file = File(dataFolder, "teams.yml")
        val configuration = YamlConfiguration.loadConfiguration(file)

        val teams = configuration.getConfigurationSection("teams")
        teams?.getKeys(false)?.forEach { key ->
            game.registerTeam(
                GameTeam(
                    game,
                    teams.getString("$key.name")!!,
                    MiniMessage.miniMessage().deserialize(teams.getString("$key.displayName")!!),
                    TextColor.fromHexString(teams.getString("$key.color")!!) ?: NamedTextColor.WHITE,
                    teams.getInt("$key.size"),
                    Material.getMaterial(teams.getString("$key.icon")!!) ?: Material.WHITE_BANNER,
                    GameBeaconDeposit(
                        LocationSerialization.deserialize(teams.getString("$key.beaconDeposit.location")!!),
                        teams.getDouble("$key.beaconDeposit.radius"),
                        LocationSerialization.deserialize(teams.getString("$key.beaconDeposit.blockLocation")!!)
                    ),
                    LocationSerialization.deserialize(teams.getString("$key.spawn")!!),
                )
            )
        }

        LOGGER.info("Loaded ${game.getTeams().size} teams")
    }

    private fun addEvents(vararg listeners: Listener) {
        listeners.forEach { this.server.pluginManager.registerEvents(it, this) }
    }
}