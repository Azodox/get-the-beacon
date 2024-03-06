package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.event.team.GamePlayerJoinsTeamEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.lang.language
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

private const val LOBBY_TEAM_CHOICE_JOINED = "lobby.team.choice.joined"

class GamePlayerJoinsTeamListener(private val game: Game) : Listener {

    @EventHandler
    fun onGamePlayerJoinsTeam(event: GamePlayerJoinsTeamEvent) {
        val player = event.player
        val team = event.team

        player.sendActionBar(language(player).format(LOBBY_TEAM_CHOICE_JOINED, team.name))

        if (game.getTeams().sumOf { it.players.size } >= game.minPlayers) {
            game.start()
        }
    }
}