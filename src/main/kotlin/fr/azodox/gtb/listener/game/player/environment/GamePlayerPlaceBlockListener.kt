package fr.azodox.gtb.listener.game.player.environment

import fr.azodox.gtb.event.game.player.GamePlayerGenericLobbyEvent
import fr.azodox.gtb.game.Game
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class GamePlayerPlaceBlockListener(private val game: Game) : Listener {

    @EventHandler
    fun onGamePlayerPlaceBlock(event: BlockPlaceEvent) {
        val genericEvent = GamePlayerGenericLobbyEvent(game, event.player)
        Bukkit.getPluginManager().callEvent(genericEvent)
        event.isCancelled = genericEvent.isCancelled
    }
}