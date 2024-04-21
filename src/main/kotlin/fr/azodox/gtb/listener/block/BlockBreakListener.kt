package fr.azodox.gtb.listener.block

import fr.azodox.gtb.game.Game
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class BlockBreakListener(private val game: Game) : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.BEACON && game.beacon.block.location == block.location) {
            event.isCancelled = true
        }
    }
}