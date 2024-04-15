package fr.azodox.gtb.listener.game.beacon

import fr.azodox.gtb.event.game.beacon.GameBeaconTakesDamageEvent
import fr.azodox.gtb.game.GameBeaconState
import fr.azodox.gtb.util.ProgressBarUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GameBeaconTakesDamageListener : Listener {

    @EventHandler
    fun onBeaconTakesDamage(event: GameBeaconTakesDamageEvent) {
        val beacon = event.beacon
        val damager = event.damager

        if (beacon.state == GameBeaconState.BASE && beacon.owningTeam == beacon.game.getPlayerTeam(damager)) {
            event.isCancelled = true
            return
        }

        damager.sendActionBar(
            Component.text("[").color(NamedTextColor.DARK_GRAY).append(
                Component.text(
                    ProgressBarUtil.getProgressBar(
                        beacon.health,
                        beacon.defaultHealth,
                        60,
                        '|',
                        "§a",
                        "§7"
                    )
                )
            ).append(Component.text("]")).color(NamedTextColor.DARK_GRAY)
        )

        if (beacon.health - event.damage <= beacon.defaultHealth * 0.1){
            event.isCancelled = true
            beacon.health = beacon.defaultHealth * 0.1 // Prevents the beacon from being destroyed
            beacon.slime.health = beacon.health
            if (beacon.state == GameBeaconState.BASE) {
                beacon.returnToDefaultLocation()
            } else
                beacon.triggerProtection()
        }

    }
}