package fr.azodox.gtb

import co.aikar.commands.PaperCommandManager
import fr.azodox.gtb.commands.LanguageCommand
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.lang.LanguageCore
import fr.azodox.gtb.listener.game.player.GamePlayerInitializationListener
import fr.azodox.gtb.listener.PlayerJoinListener
import fr.azodox.gtb.listener.PlayerQuitListener
import fr.azodox.gtb.listener.game.player.GamePlayerRemovedListener
import fr.azodox.gtb.listener.inventory.PlayerInteractionListener
import fr.azodox.gtb.util.LocationSerialization
import me.devnatan.inventoryframework.ViewFrame
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
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
        copyFiles(
            arrayOf(
                "teams.yml"
            )
        )

        languageCore.init()
        game = Game(this)
        viewFrame = ViewFrame.create(this)

        loadTeams()

        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(this), this)
        server.pluginManager.registerEvents(GamePlayerInitializationListener(), this)
        server.pluginManager.registerEvents(GamePlayerRemovedListener(), this)
        server.pluginManager.registerEvents(PlayerInteractionListener(game), this)

        val manager = PaperCommandManager(this)
        manager.commandCompletions.registerAsyncCompletion("locales") {
            LanguageCore.languages.map { it.key }
        }
        manager.registerCommand(LanguageCommand(languageCore))

        LOGGER.info("Enabled!")
    }

    private fun copyFiles(files: Array<String>) {
        files.forEach {
            if (!dataFolder.exists()) dataFolder.mkdir()

            val file = File(dataFolder, it)
            if (!file.exists()) file.createNewFile()

            if (file.length() != 0L) return

            file.outputStream().use { output ->
                getResource(it)?.copyTo(output)
                LOGGER.info("Copied $it to data folder!")
            }
        }
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
                    TextColor.color(teams.getString("$key.color")!!.toInt(16)),
                    teams.getInt("$key.size"),
                    Material.getMaterial(teams.getString("$key.icon")!!) ?: Material.WHITE_BANNER,
                    LocationSerialization.deserialize(teams.getString("$key.spawn")!!),
                )
            )
        }

        LOGGER.info("Loaded ${game.getTeams().size} teams!")
    }
}