package fr.azodox.gtb.event.game.player

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import org.bukkit.OfflinePlayer

open class GamePlayerRemovedEvent(game: Game, open val player: OfflinePlayer) : GameEvent(game) {
}