package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.event.team.GamePlayerLeavesTeamEvent
import fr.azodox.gtb.lang.language
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

private const val LOBBY_TEAM_CHOICE_LEFT = "lobby.team.choice.left"

class GamePlayerLeavesTeamListener : Listener {

    @EventHandler
    fun onGamePlayerLeavesTeam(event: GamePlayerLeavesTeamEvent) {
        val player = event.player
        val team = event.team
        player.sendActionBar(language(player).format(LOBBY_TEAM_CHOICE_LEFT, team.name))
    }
}