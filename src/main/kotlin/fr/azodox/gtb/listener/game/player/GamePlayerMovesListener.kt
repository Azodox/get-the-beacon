package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeaconState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class GamePlayerMovesListener(private val game: Game) : Listener {

    @EventHandler
    fun onPlayerMoves(event: PlayerMoveEvent) {
        val player = event.player
        val beacon = game.beacon
        if (beacon.state == GameBeaconState.PICKED_UP) {
            val display = beacon.pickedUpBeacons[player.uniqueId] ?: return
            display.interpolationDuration = 10
            display.interpolationDelay = -1
            val location = player.eyeLocation.subtract(player.eyeLocation.direction)
            location.y += 1.0
            location.x += 0.5
            location.z += 0.5
            display.teleport(location)
        }
    }
}