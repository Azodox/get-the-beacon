package fr.azodox.gtb.event.team

import fr.azodox.gtb.event.GameEvent
import fr.azodox.gtb.game.Game
import fr.azodox.gtb.game.team.GameTeam
import org.bukkit.entity.Player

class GamePlayerLeavesTeamEvent(game: Game, val player: Player, val team: GameTeam) : GameEvent(game) {
}