package fr.azodox.gtb.listener.game.player.environment

import fr.azodox.gtb.event.game.player.GamePlayerGenericLobbyEvent
import fr.azodox.gtb.game.Game
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class GamePlayerPickupItemListener(private val game: Game) : Listener {

    @EventHandler
    fun onGamePlayerPickupItem(event: EntityPickupItemEvent) {
        val entity = event.entity
        if (entity !is org.bukkit.entity.Player) return
        val genericEvent = GamePlayerGenericLobbyEvent(game, entity)
        Bukkit.getPluginManager().callEvent(genericEvent)
        event.isCancelled = genericEvent.isCancelled
    }
}