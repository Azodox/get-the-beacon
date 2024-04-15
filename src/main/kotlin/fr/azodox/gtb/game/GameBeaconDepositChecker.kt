package fr.azodox.gtb.game

import fr.azodox.gtb.game.team.GameBeaconDeposit
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.CacheHelper
import fr.azodox.gtb.util.lerp
import org.bukkit.Sound
import java.util.*

class GameBeaconDepositChecker(private val beacon: GameBeacon, private val deposit: GameBeaconDeposit) : Runnable {

    private var countdown: Int = 10
    private val baseCountdown = countdown

    override fun run() {
        val nearbyPlayers = deposit.location.getNearbyPlayers(deposit.radius, deposit.radius)
        if (nearbyPlayers.isEmpty() || nearbyPlayers.size > 1) {
            countdown = 10
            return
        }

        val player = nearbyPlayers.first()
        val holder = CacheHelper.get<UUID>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder")
        if (holder != player.uniqueId) {
            return
        }

        countdown--
        if (countdown > 0) {
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, lerp(1.0f, 0.0f, countdown.toFloat() / baseCountdown.toFloat()))
            player.sendMessage(language(player).format("game.beacon.deposit-checker.countdown", countdown.toString()))
        }

        if (countdown <= 0) {
            beacon.deposit(player)
        }
    }
}