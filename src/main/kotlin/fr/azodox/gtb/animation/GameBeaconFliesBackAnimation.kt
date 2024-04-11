package fr.azodox.gtb.animation

import fr.azodox.gtb.game.GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT
import fr.azodox.gtb.game.GameBeacon
import fr.azodox.gtb.util.CacheHelper
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.scheduler.BukkitRunnable

class GameBeaconFliesBackAnimation(private val beacon: GameBeacon, private val targetLocation: Location) : BukkitRunnable() {

    private var timer = 0

    override fun run() {
        val display = CacheHelper.get<BlockDisplay>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display") ?: return
        display.transformation
        timer++
    }
}