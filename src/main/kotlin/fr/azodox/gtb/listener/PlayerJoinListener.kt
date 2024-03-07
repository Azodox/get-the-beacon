package fr.azodox.gtb.listener

import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.game.GameState
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val gtb: GetTheBeacon) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val game = gtb.game

        gtb.languageCore.initPlayer(player)
        when (game.state) {
            GameState.WAITING, GameState.STARTING -> {
                game.initPlayer(player.uniqueId)
                event.joinMessage(Component.empty())
            }

            else -> {}
        }
    }
}