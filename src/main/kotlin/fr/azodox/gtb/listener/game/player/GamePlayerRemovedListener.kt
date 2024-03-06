package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.event.game.player.GamePlayerRemovedEvent
import fr.azodox.gtb.lang.language
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GamePlayerRemovedListener : Listener {

    @EventHandler
    fun onGamePlayerRemoved(event: GamePlayerRemovedEvent) {
        val player = event.player
        event.game.getWaitingPlayers().filter { it.uniqueId != player.uniqueId }.forEach {
            it.sendActionBar(
                language(it).format("lobby.player.quit", player.name ?: "")
            )
        }
    }
}