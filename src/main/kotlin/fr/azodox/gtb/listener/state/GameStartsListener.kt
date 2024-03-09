package fr.azodox.gtb.listener.state

import fr.azodox.gtb.event.game.GameStartsEvent
import fr.azodox.gtb.game.GameState
import fr.azodox.gtb.game.team.GameTeam
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GameStartsListener : Listener {

    @EventHandler
    fun onGameStarts(event: GameStartsEvent) {
        event.game.getTeams().forEach(GameTeam::spawnPlayers)
        event.game.gamePlayers.forEach { event.game.registerPlayerBoard(Bukkit.getPlayer(it)!!) }

        Bukkit.getScheduler().runTaskTimer(event.game.plugin, Runnable {
            if (event.game.state == GameState.IN_GAME) {
                val playerBoards = event.game.playerBoards;

                for ((_, board) in playerBoards) {
                    if (Bukkit.getOnlinePlayers().contains(board.player)) {
                        event.game.updatePlayerBoard(board)
                    }
                }
            }
        }, 0, 20)
    }
}