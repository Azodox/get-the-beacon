package fr.azodox.gtb.game.team

import fr.azodox.gtb.event.team.GamePlayerJoinsTeamEvent
import fr.azodox.gtb.event.team.GamePlayerLeavesTeamEvent
import fr.azodox.gtb.game.Game
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.*

data class GameTeam(
    private val game: Game,
    val name: String,
    val displayName: Component,
    val color: TextColor,
    val size: Int,
    val icon: Material,
    private val spawn: Location,
    val players: MutableList<UUID> = mutableListOf()
) {

    private var bukkitTeam: Team

    init {
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        bukkitTeam = scoreboard.getTeam(name) ?: scoreboard.registerNewTeam(name)
        bukkitTeam.displayName(displayName)
        bukkitTeam.color(NamedTextColor.nearestTo(displayName.color()!!))
        bukkitTeam.prefix(displayName.appendSpace())
        bukkitTeam.setAllowFriendlyFire(false)
        bukkitTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
    }

    fun join(player: UUID) {
        val playerName = Bukkit.getPlayer(player)?.name ?: return
        players.add(player)
        bukkitTeam.addEntry(playerName)
        Bukkit.getPluginManager().callEvent(GamePlayerJoinsTeamEvent(game, Bukkit.getPlayer(player)!!, this))
    }

    fun leave(player: Player) {
        players.remove(player.uniqueId)
        bukkitTeam.removeEntry(player.name)
        Bukkit.getPluginManager().callEvent(GamePlayerLeavesTeamEvent(game, player, this))
    }

    fun contains(player: UUID) = players.contains(player)

    fun spawnPlayers() {
        players.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.teleportAsync(spawn)
        }
    }
}