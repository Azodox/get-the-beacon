package fr.azodox.gtb.listener.state

import fr.azodox.gtb.event.game.GamePhaseChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GamePhaseChangeListener : Listener {

    @EventHandler
    fun onGamePhaseChange(event: GamePhaseChangeEvent) {
        val game = event.game
        when (game.currentPhase) {
            1 -> {
                // Phase 1 related initialization
            }
            2 -> {
                // Phase 2 related initialization
            }
            else -> throw IllegalStateException("Unknown phase: ${game.currentPhase}")
        }
    }
}