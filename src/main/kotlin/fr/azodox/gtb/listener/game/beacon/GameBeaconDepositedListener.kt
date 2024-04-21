package fr.azodox.gtb.listener.game.beacon

import fr.azodox.gtb.event.game.beacon.GameBeaconDepositedEvent
import fr.azodox.gtb.lang.language
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

private const val GAME_BEACON_DEPOSITED_BROADCAST_KEY = "game.beacon.deposited.broadcast"

class GameBeaconDepositedListener : Listener {

    @EventHandler
    fun onGameBeaconDeposited(event: GameBeaconDepositedEvent) {
        val game = event.game
        val beacon = event.beacon
        val team = beacon.owningTeam
        game.getOnlinePlayers().forEach { player ->
            player.sendMessage(
                language(player).message(GAME_BEACON_DEPOSITED_BROADCAST_KEY).replaceText {
                    it.match("%player%").replacement(event.depositor.displayName().color(team.color))
                }.replaceText {
                    it.match("%team%").replacement(team.displayName)
                }
            )
            player.playSound(team.beaconDeposit.location, Sound.ENTITY_WITHER_SPAWN, 0.5F, 1.0F)
        }
    }
}