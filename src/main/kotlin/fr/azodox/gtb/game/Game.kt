package fr.azodox.gtb.game

import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.event.game.GameStateChangeEvent
import fr.azodox.gtb.event.game.player.GamePlayerInitializationEvent
import fr.azodox.gtb.event.game.player.GamePlayerRemovedEvent
import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.game.team.view.GameTeamChoiceView
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.LocationSerialization
import me.devnatan.inventoryframework.ViewFrame
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*

private const val GAME_BEACON_LOCATION_CONFIG_KEY = "game.beacon.spawn"

private const val GAME_BEACON_DEFAULT_HEALTH_CONFIG_KEY = "game.beacon.default-health"

data class Game(
    val plugin: GetTheBeacon,
    val id: String = UUID.randomUUID().toString().replace("-", "").substring(5, 10),
    val name: String = "GetTheBeacon $id",
    var minPlayers: Int = 2,
) {

    var state: GameState = GameState.WAITING
        set(value) {
            val previous = field
            field = value
            Bukkit.getPluginManager().callEvent(GameStateChangeEvent(this, previous, value))
        }

    private val waitingPlayers: MutableList<UUID> = mutableListOf()
    private val gamePlayers: MutableList<UUID> = mutableListOf()
    private val teams: MutableList<GameTeam> = mutableListOf()

    private var countDownTask: BukkitTask? = null
    private var currentTeamThreshold = 1

    var beacon: GameBeacon = GameBeacon(
        this,
        LocationSerialization.deserialize(plugin.config.getString(GAME_BEACON_LOCATION_CONFIG_KEY)!!),
        GameBeaconState.CENTER,
        plugin.config.getDouble(GAME_BEACON_DEFAULT_HEALTH_CONFIG_KEY)
    )

    init {
        beacon.spawnAtDefaultLocation()
    }

    fun start() {
        countDownTask?.cancel()

        if (teams.isEmpty())
            throw IllegalStateException("No team registered")

        if (teams.size < 2)
            throw IllegalStateException("Not enough teams registered")

        state = GameState.STARTING
        var time = plugin.config.getInt("game.starting-time")
        countDownTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (teams.sumOf { it.players.size } < minPlayers) {
                state = GameState.WAITING
                waitingPlayers.forEach {
                    Bukkit.getPlayer(it)?.sendMessage(language(it).message("start.not-enough-players").color(NamedTextColor.RED))
                }

                Bukkit.getScheduler().cancelTask(countDownTask!!.taskId)
            }

            if (time == 0) {
                state = GameState.IN_GAME
                gamePlayers.addAll(teams.map { it.players }.flatten())
                Bukkit.getScheduler().cancelTask(countDownTask!!.taskId)
                return@Runnable
            }

            waitingPlayers.forEach {
                Bukkit.getPlayer(it)?.sendActionBar(language(it).format("start.starting", time.toString()))
            }

            time--
        }, 0, 20)
    }

    fun registerTeam(team: GameTeam) {
        if (teams.contains(team)) {
            throw IllegalArgumentException("Team '${team.name}' already registered")
        }
        teams.add(team)
        GetTheBeacon.LOGGER.info("Registered team '${team.name}'")
    }

    fun initPlayer(player: UUID) {
        waitingPlayers.add(player)
        Bukkit.getPluginManager().callEvent(GamePlayerInitializationEvent(this, Bukkit.getPlayer(player)!!))
    }

    fun removePlayer(player: UUID) {
        waitingPlayers.remove(player)
        Bukkit.getPluginManager().callEvent(GamePlayerRemovedEvent(this, Bukkit.getPlayer(player)!!))
    }

    fun getWaitingPlayers(): List<Player> {
        return waitingPlayers.map { Bukkit.getPlayer(it)!! }.toList()
    }

    fun getOnlinePlayers(): List<Player> {
        return gamePlayers.mapNotNull { Bukkit.getPlayer(it) }.filter { it.isOnline }.toList()
    }

    fun getPlayerTeam(player: Player): GameTeam? {
        return teams.find { it.players.contains(player.uniqueId) }
    }

    fun getTeams(): List<GameTeam> {
        return teams.toList()
    }

    fun switchToRandomTeam(player: Player) {
        val previousTeam = getPlayerTeam(player)
        val newTeam = getBestMatchingTeamForPlayer(player) ?: return
        previousTeam?.leave(player)
        newTeam.join(player.uniqueId)
        checkThreshold()
    }

    private fun checkThreshold() {
        teams.maxByOrNull { it.players.size }?.let {
            if (it.players.size > currentTeamThreshold) {
                currentTeamThreshold = it.players.size
            }
        }
    }

    private fun getBestMatchingTeamForPlayer(player: Player): GameTeam? {
        val previousTeam = getPlayerTeam(player)
        return teams.filter { it != previousTeam && it.players.size < currentTeamThreshold }.minByOrNull { it.size }
    }

    fun openTeamChoiceView(player: Player) {
        val viewFrame = ViewFrame.create(plugin)
        viewFrame.with(GameTeamChoiceView(this.teams)).register()
        viewFrame.open(GameTeamChoiceView::class.java, player)
    }
}