package fr.azodox.gtb.listener

import fr.azodox.gtb.GetTheBeacon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val gtb: GetTheBeacon) : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        gtb.game.removePlayer(event.player.uniqueId)
    }
}