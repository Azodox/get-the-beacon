package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.game.GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeaconState
import fr.azodox.gtb.util.CacheHelper
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class GamePlayerMovesListener(private val game: Game) : Listener {

    @EventHandler
    fun onPlayerMoves(event: PlayerMoveEvent) {
        val player = event.player
        val beacon = game.beacon
        if (beacon.state == GameBeaconState.PICKED_UP && CacheHelper.compare(
                GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder",
                player.uniqueId
            )
        ) {
            val display = CacheHelper.get<BlockDisplay>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display") ?: return
            val location = player.eyeLocation.subtract(player.eyeLocation.direction)
            location.y += 1.0
            location.x += 0.5
            location.z += 0.5
            display.teleport(location)
        }
    }
}