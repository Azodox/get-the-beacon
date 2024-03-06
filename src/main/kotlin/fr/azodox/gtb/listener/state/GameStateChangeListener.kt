package fr.azodox.gtb.listener.state

import fr.azodox.gtb.event.game.GameStartsEvent
import fr.azodox.gtb.event.game.GameStateChangeEvent
import fr.azodox.gtb.game.GameState
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GameStateChangeListener : Listener {

    @EventHandler
    fun onGameStateChange(event: GameStateChangeEvent) {
        when(event.value) {
            GameState.IN_GAME -> {
                Bukkit.getPluginManager().callEvent(GameStartsEvent(event.game))
            }
            else -> {}
        }
    }
}