package fr.azodox.gtb.event.game.beacon

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeacon
import org.bukkit.event.Cancellable

class GameBeaconTakesDamageEvent(game: Game, val beacon: GameBeacon, val damage: Double) : GameEvent(game), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}