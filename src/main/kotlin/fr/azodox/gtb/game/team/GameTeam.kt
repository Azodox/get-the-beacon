package fr.azodox.gtb.game.team

import fr.azodox.gtb.event.team.GamePlayerJoinsTeamEvent
import fr.azodox.gtb.event.team.GamePlayerLeavesTeamEvent
import fr.azodox.gtb.game.Game
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import java.util.*

data class GameTeam(
    private val game: Game,
    val name: String,
    val displayName: Component,
    val color: TextColor,
    val size: Int,
    val icon: Material,
    val beaconDeposit: GameBeaconDeposit,
    private val spawn: Location,
    val players: MutableList<UUID> = mutableListOf()
) {

    private var bukkitTeam: Team

    init {
        beaconDeposit.team = this

        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        bukkitTeam = scoreboard.getTeam(name) ?: scoreboard.registerNewTeam(name)
        bukkitTeam.displayName(displayName)
        bukkitTeam.color(NamedTextColor.nearestTo(displayName.color() ?: NamedTextColor.WHITE))
        bukkitTeam.prefix(displayName.appendSpace())
        bukkitTeam.setAllowFriendlyFire(false)
        bukkitTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
    }

    fun join(player: UUID) {
        val playerName = Bukkit.getPlayer(player)?.name ?: return
        players.add(player)
        bukkitTeam.addEntry(playerName)
        Bukkit.getPluginManager().callEvent(GamePlayerJoinsTeamEvent(game, Bukkit.getPlayer(player)!!, this))
    }

    fun leave(player: Player) {
        players.remove(player.uniqueId)
        bukkitTeam.removeEntry(player.name)
        Bukkit.getPluginManager().callEvent(GamePlayerLeavesTeamEvent(game, player, this))
    }

    fun contains(player: UUID) = players.contains(player)

    fun spawnPlayers() {
        players.mapNotNull(Bukkit::getPlayer).forEach { player ->
            player.inventory.clear()
            player.teleportAsync(spawn)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameTeam

        return game == other.game &&
                name == other.name &&
                displayName == other.displayName &&
                bukkitTeam == other.bukkitTeam &&
                players == other.players &&
                beaconDeposit == other.beaconDeposit &&
                icon == other.icon &&
                size == other.size &&
                spawn == other.spawn &&
                color == other.color
    }

    override fun hashCode(): Int {
        var result = game.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + size
        result = 31 * result + icon.hashCode()
        result = 31 * result + beaconDeposit.hashCode()
        result = 31 * result + spawn.hashCode()
        result = 31 * result + players.hashCode()
        result = 31 * result + bukkitTeam.hashCode()
        return result
    }

}

data class GameBeaconDeposit(val location: Location, val radius: Double, val blockLocation: Location){
    lateinit var team: GameTeam
}