package fr.azodox.gtb.listener.game.player.state

import fr.azodox.gtb.event.game.player.GamePlayerGenericLobbyEvent
import fr.azodox.gtb.game.Game
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class GamePlayerFoodLevelChangeListener(private val game: Game) : Listener {

    @EventHandler
    fun onGamePlayerFoodLevelChange(event: FoodLevelChangeEvent) {
        val p = event.entity
        if (p !is org.bukkit.entity.Player) return
        val genericEvent = GamePlayerGenericLobbyEvent(game, p)
        Bukkit.getPluginManager().callEvent(genericEvent)
        event.isCancelled = genericEvent.isCancelled
    }
}