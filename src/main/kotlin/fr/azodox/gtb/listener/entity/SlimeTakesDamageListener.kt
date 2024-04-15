package fr.azodox.gtb.listener.entity

import fr.azodox.gtb.event.game.beacon.GameBeaconTakesDamageEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeaconState
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class SlimeTakesDamageListener(private val game: Game) : Listener {

    @EventHandler
    fun onSlimeTakesDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entityType != EntityType.SLIME) return
        if (event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.isCancelled = true
            return
        }
        if (event.damager !is Player) {
            event.isCancelled = true
            return
        }

        val beacon = game.beacon
        if (beacon.state == GameBeaconState.PROTECTED) {
            event.isCancelled = true
            return
        }

        val slime = event.entity as Slime
        beacon.health = slime.health

        val beaconTakesDamageEvent = GameBeaconTakesDamageEvent(game, beacon, event.damager as Player, event.damage)
        Bukkit.getPluginManager().callEvent(beaconTakesDamageEvent)
        event.isCancelled = beaconTakesDamageEvent.isCancelled
    }

    @EventHandler
    fun onSlimeTakesDamage(event: EntityDamageEvent){
        if (event.entityType != EntityType.SLIME) return
        event.isCancelled = (event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
    }
}