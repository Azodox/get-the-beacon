package fr.azodox.gtb.listener.inventory

import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameState
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class PlayerInteractionListener(private val game: Game, private val javaPlugin: Plugin) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action.isRightClick && event.hand!! == EquipmentSlot.HAND) {
            val item = event.item ?: return
            val player = event.player

            when (game.state) {
                GameState.WAITING, GameState.STARTING -> {
                    if (item.itemMeta.persistentDataContainer.has(NamespacedKey(javaPlugin, "teamselectobject"), PersistentDataType.STRING))
                        game.switchToRandomTeam(player)
                }

                GameState.IN_GAME -> TODO()
                GameState.ENDING -> TODO()
            }
        }
    }
}