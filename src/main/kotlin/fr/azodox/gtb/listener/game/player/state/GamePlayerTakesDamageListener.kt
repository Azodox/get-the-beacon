package fr.azodox.gtb.listener.game.player.state

import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class GamePlayerTakesDamageListener(private val game: Game) : Listener {

    @EventHandler
    fun onPlayerTakeDamageFromPlayer(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity !is Player) return

        val takesDamage = when (game.state) {
            GameState.WAITING, GameState.STARTING -> event.damager is Player
            else -> true
        }

        if (!takesDamage) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerTakeDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is Player) return

        if (event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            val takesDamage = when (game.state) {
                GameState.WAITING, GameState.STARTING -> false
                else -> true
            }

            if (!takesDamage) event.isCancelled = true
        }
    }
}