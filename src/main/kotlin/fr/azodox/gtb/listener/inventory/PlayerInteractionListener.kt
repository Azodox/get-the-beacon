package fr.azodox.gtb.listener.inventory

import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameState
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class PlayerInteractionListener(private val game: Game) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action.isRightClick && event.hand!! == EquipmentSlot.HAND) {
            val item = event.item ?: return
            val player = event.player

            when (item.type) {
                Material.WHITE_BANNER -> {
                    if (game.state == GameState.WAITING) {

                    }
                }

                else -> {}
            }
        }
    }
}