package fr.azodox.gtb.animation

import fr.azodox.gtb.game.GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT
import fr.azodox.gtb.game.GameBeacon
import fr.azodox.gtb.util.CacheHelper
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.BlockDisplay
import org.bukkit.scheduler.BukkitRunnable

class GameBeaconFliesBackAnimation(private val beacon: GameBeacon, private val targetLocation: Location) : BukkitRunnable() {

    private val display = CacheHelper.get<BlockDisplay>(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display")

    override fun run() {
        if (display == null){
            cancel()
            return
        }

        val location = display.location
        if (targetLocation.getNearbyEntitiesByType(BlockDisplay::class.java, 0.5, 1.0).contains(display)){
            beacon.spawnAtDefaultLocation()
            display.remove()
            CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_display")
            CacheHelper.remove(GAME_BEACON_PICKED_UP_CACHE_PREFIX_CONSTANT + "_holder")
            cancel()
            return
        }

        val vector = targetLocation.toVector().subtract(location.toVector()).normalize()
        location.add(vector).add(0.0, 0.5, 0.0)
        display.teleport(location)
        location.world.spawnParticle(Particle.CLOUD, location, 20, 0.0, 0.0, 0.0, 0.0)
    }
}