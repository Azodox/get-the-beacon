package fr.azodox.gtb.event.team

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.team.GameTeam
import org.bukkit.entity.Player

open class GamePlayerJoinsTeamEvent(game: Game, open val player: Player, open val team: GameTeam) : GameEvent(game) {
}