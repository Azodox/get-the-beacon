package fr.azodox.gtb.game.team

import fr.azodox.gtb.event.team.GamePlayerJoinsTeamEvent
import fr.azodox.gtb.event.team.GamePlayerLeavesTeamEvent
import fr.azodox.gtb.game.Game
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scoreboard.Team
import java.util.*

data class GameTeam(
    private val game: Game,
    val name: String,
    val displayName: Component,
    val color: TextColor,
    val size: Int,
    private val spawn: Location,
    private val players: MutableList<UUID> = mutableListOf()
) {

    private lateinit var bukkitTeam: Team

    fun join(player: UUID){
        players.add(player)
        bukkitTeam.addEntry(player.toString())
        Bukkit.getPluginManager().callEvent(GamePlayerJoinsTeamEvent(game, Bukkit.getPlayer(player)!!, this))
    }

    fun leave(player: UUID) {
        players.remove(player)
        bukkitTeam.removeEntry(player.toString())
        Bukkit.getPluginManager().callEvent(GamePlayerLeavesTeamEvent(game, Bukkit.getPlayer(player)!!, this))
    }

    fun spawnPlayers() {
        players.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.teleportAsync(spawn)
        }
    }
}