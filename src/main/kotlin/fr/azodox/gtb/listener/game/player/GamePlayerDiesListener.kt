package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.game.GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeaconState
import fr.azodox.gtb.game.GameState
import fr.azodox.gtb.util.CacheHelper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class GamePlayerDiesListener(private val game: Game) : Listener {

    @EventHandler
    fun onPlayerDies(event: PlayerDeathEvent) {
        if (game.state != GameState.IN_GAME) return
        val player = event.entity
        if (game.beacon.state == GameBeaconState.PICKED_UP && CacheHelper.compare(
                GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder",
                player.uniqueId
            )
        ) {
            game.beacon.drop(player, player.location)
        }
    }
}