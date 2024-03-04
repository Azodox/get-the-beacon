package fr.azodox.gtb.event.game

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameState

open class GameStateChangeEvent(game: Game, open val previousValue: GameState, open val value: GameState) : GameEvent(game) {

}
