package fr.azodox.gtb.listener.game.beacon

import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeaconState
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class GameBeaconPickUpListener(private val game: Game) : Listener {

    @EventHandler
    fun onGameBeaconPickUp(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock ?: return
        val beacon = game.beacon

        if (beacon.state == GameBeaconState.VULNERABLE && block.type == Material.BEACON && block.location == beacon.block.location) {
            event.isCancelled = true
            beacon.pickUp(player)
        }
    }
}