package fr.azodox.gtb.event

import fr.azodox.gtb.game.Game
import org.bukkit.entity.Player

open class GamePlayerInitializationEvent(game: Game, open val player: Player) : GameEvent(game) {


}