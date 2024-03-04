package fr.azodox.gtb.game

import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.event.game.player.GamePlayerInitializationEvent
import fr.azodox.gtb.event.game.GameStateChangeEvent
import fr.azodox.gtb.event.game.player.GamePlayerRemovedEvent
import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.lang.LanguageCore
import fr.azodox.gtb.lang.language
import me.devnatan.inventoryframework.ViewFrame
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

data class Game(
    val id: String = UUID.randomUUID().toString().replace("-", "").substring(5, 10),
    val name: String = "GetTheBeacon $id",
    private val waitingPlayers: MutableList<UUID> = mutableListOf(),
    private val teams: MutableList<GameTeam> = mutableListOf()
) {

    var state: GameState = GameState.WAITING
        set(value) {
            val previous = field
            field = value
            Bukkit.getPluginManager().callEvent(GameStateChangeEvent(this, previous, value))
        }

    fun start(sender: CommandSender){
        val language = if (sender is Player) language(sender) else LanguageCore.DEFAULT_LANGUAGE

        if(teams.isEmpty())
            throw IllegalStateException("No team registered")

        if(teams.size < 2)
            throw IllegalStateException("Not enough teams registered")

        if(waitingPlayers.size < 2) {
            sender.sendMessage(
                language.message("start.not-enough-players").color(NamedTextColor.RED)
            )
            return
        }

        state = GameState.STARTING
        // TODO: COUNTDOWN + START
    }

    fun registerTeam(team: GameTeam) {
        if(teams.contains(team)) {
            throw IllegalArgumentException("Team '${team.name}' already registered")
        }
        teams.add(team)
        GetTheBeacon.LOGGER.info("Registered team '${team.name}'")
    }

    fun initPlayer(player: UUID) {
        waitingPlayers.add(player)
        Bukkit.getPluginManager().callEvent(GamePlayerInitializationEvent(this, Bukkit.getPlayer(player)!!))
    }

    fun removePlayer(player: UUID) {
        waitingPlayers.remove(player)
        Bukkit.getPluginManager().callEvent(GamePlayerRemovedEvent(this, Bukkit.getOfflinePlayer(player)))
    }

    fun getWaitingPlayers(): List<Player> {
        return waitingPlayers.map { Bukkit.getPlayer(it)!! }.toList()
    }

    fun getTeams(): List<GameTeam> {
        return teams.toList()
    }
}