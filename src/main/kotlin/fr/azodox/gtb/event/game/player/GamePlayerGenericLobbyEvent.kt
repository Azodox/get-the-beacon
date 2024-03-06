package fr.azodox.gtb.event.game.player

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable

class GamePlayerGenericLobbyEvent(game: Game, val player: Player) : GameEvent(game), Cancellable {
    private var cancel = true

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(shouldCancel: Boolean) {
        cancel = shouldCancel
    }
}