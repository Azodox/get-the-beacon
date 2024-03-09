package fr.azodox.gtb.event.game

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game

class GamePhaseChangeEvent(game: Game, val previousValue: Int, val value: Int) : GameEvent(game) {
}