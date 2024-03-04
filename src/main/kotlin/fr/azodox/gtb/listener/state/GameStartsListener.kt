package fr.azodox.gtb.listener.state

import fr.azodox.gtb.event.game.GameStartsEvent
import fr.azodox.gtb.game.team.GameTeam
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GameStartsListener : Listener {

    @EventHandler
    fun onGameStarts(event: GameStartsEvent) {
        event.game.getTeams().forEach(GameTeam::spawnPlayers)
    }
}