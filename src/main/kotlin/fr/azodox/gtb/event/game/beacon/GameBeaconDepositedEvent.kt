package fr.azodox.gtb.event.game.beacon

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.GameBeacon
import org.bukkit.entity.Player

class GameBeaconDepositedEvent(game: Game, val beacon: GameBeacon, val depositor: Player) : GameEvent(game) {
}