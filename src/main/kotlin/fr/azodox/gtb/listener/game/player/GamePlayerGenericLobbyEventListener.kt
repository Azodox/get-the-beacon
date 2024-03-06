package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.event.game.player.GamePlayerGenericLobbyEvent
import fr.azodox.gtb.game.GameState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GamePlayerGenericLobbyEventListener : Listener {

    @EventHandler
    fun onGenericLobbyEvent(event: GamePlayerGenericLobbyEvent) {
        event.isCancelled = (event.game.state == GameState.WAITING || event.game.state == GameState.STARTING)
    }
}