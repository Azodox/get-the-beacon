package fr.azodox.gtb.game

import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.event.game.GamePhaseChangeEvent
import fr.azodox.gtb.event.game.GameStateChangeEvent
import fr.azodox.gtb.event.game.player.GamePlayerInitializationEvent
import fr.azodox.gtb.event.game.player.GamePlayerRemovedEvent
import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.game.team.view.GameTeamChoiceView
import fr.azodox.gtb.lang.language
import fr.mrmicky.fastboard.adventure.FastBoard
import me.devnatan.inventoryframework.ViewFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*


data class Game(
    val plugin: GetTheBeacon,
    val id: String = UUID.randomUUID().toString().replace("-", "").substring(5, 10),
    val name: String = "GetTheBeacon $id",
    private val waitingPlayers: MutableList<UUID> = mutableListOf(),
    val gamePlayers: MutableList<UUID> = mutableListOf(),
    val playerBoards: MutableMap<UUID, FastBoard> = mutableMapOf(),
    var minPlayers: Int = 2,
    private val teams: MutableList<GameTeam> = mutableListOf()
) {

    var currentPhase: Int = 0
        set(value) {
            val previous = field
            field = value
            Bukkit.getPluginManager().callEvent(GamePhaseChangeEvent(this, previous, value))
        }

    var state: GameState = GameState.WAITING
        set(value) {
            val previous = field
            field = value
            Bukkit.getPluginManager().callEvent(GameStateChangeEvent(this, previous, value))
        }

    private var countDownTask: BukkitTask? = null
    private var currentTeamThreshold = 1

    fun start() {
        countDownTask?.cancel()

        if (teams.isEmpty())
            throw IllegalStateException("No team registered")

        if (teams.size < 2)
            throw IllegalStateException("Not enough teams registered")

        state = GameState.STARTING
        var time = plugin.config.getInt("game.starting-time")
        countDownTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (teams.map { it.players.size }.size < 2) {
                state = GameState.WAITING
                waitingPlayers.forEach {
                    Bukkit.getPlayer(it)?.sendActionBar(language(it).message("start.not-enough-players").color(NamedTextColor.RED))
                }

                Bukkit.getScheduler().cancelTask(countDownTask!!.taskId)
            }

            if (time == 0) {
                currentPhase = 1
                gamePlayers.addAll(waitingPlayers)
                state = GameState.IN_GAME
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
        removeBoard(player)
        Bukkit.getPluginManager().callEvent(GamePlayerRemovedEvent(this, Bukkit.getPlayer(player)!!))
    }

    fun getWaitingPlayers(): List<Player> {
        return waitingPlayers.map { Bukkit.getPlayer(it)!! }.toList()
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
        checkPlayerThreshold()
    }

    fun registerPlayerBoard(player: Player) {
        val board = FastBoard(player)
        board.updateTitle(language(board.player).message(plugin.config.getString("game.scoreboard.phase-$currentPhase.title")!!))
        this.playerBoards[player.uniqueId] = board
    }

    fun removeBoard(player: UUID) {
        val playerBoard: FastBoard? = playerBoards.remove(player)
        playerBoard?.delete()
    }

    fun updatePlayerBoard(board: FastBoard) {
        val config = plugin.config
        val scoreboardLines = config.getStringList("game.scoreboard.phase-$currentPhase.lines").map { line ->
            when (line) {
                "", " ", "\n" -> Component.empty()
                else -> language(board.player).message(line)
            }
        }
        board.updateLines(scoreboardLines)
    }

    private fun checkPlayerThreshold() {
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