package fr.azodox.gtb.listener.entity

import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeaconState
import org.bukkit.NamespacedKey
import org.bukkit.entity.EnderCrystal
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class EndCrystalTakesDamageListener(val plugin: Plugin, val game: Game) : Listener {

    @EventHandler
    fun onEndCrystalTakesDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity is EnderCrystal && game.beacon.state == GameBeaconState.PROTECTED) {
            event.isCancelled = true

            val protection = game.beacon.protection
            val health = entity.persistentDataContainer[NamespacedKey(plugin, "health"), PersistentDataType.DOUBLE]
            health?.let {
                if (it - event.damage <= 0) {
                    event.isCancelled = false
                    protection.endCrystalDies(entity)
                } else {
                    entity.persistentDataContainer[NamespacedKey(plugin, "health"), PersistentDataType.DOUBLE] = it - event.damage
                    protection.updateCrystalDisplay(entity)
                }
                protection.damage(event.damage)
            }
        }
    }
}