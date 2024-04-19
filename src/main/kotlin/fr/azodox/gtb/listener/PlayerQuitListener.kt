package fr.azodox.gtb.listener

import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.game.GameState
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val gtb: GetTheBeacon) : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val game = gtb.game
        when (game.state) {
            GameState.WAITING, GameState.STARTING -> {
                game.removePlayer(event.player.uniqueId)
                event.quitMessage(Component.empty())
            }

            GameState.IN_GAME -> {
                game.removeBoard(event.player.uniqueId)
                event.quitMessage(Component.empty())
            }

            GameState.ENDING -> TODO()
        }
    }
}