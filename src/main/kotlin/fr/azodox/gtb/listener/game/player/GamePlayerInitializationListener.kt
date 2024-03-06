package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.event.game.player.GamePlayerInitializationEvent
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.ItemBuilder
import fr.azodox.gtb.util.LocationSerialization
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

private const val LOBBY_PLAYER_JOIN = "lobby.player.join"

private const val LOBBY_TEAM_CHOICE_ITEM_NAME = "lobby.team.choice.item.name"

class GamePlayerInitializationListener(private val plugin: Plugin) : Listener {

    @EventHandler
    fun onGamePlayerInitialization(event: GamePlayerInitializationEvent) {
        val player = event.player
        player.teleportAsync(LocationSerialization.deserialize(plugin.config.getString("spawn")))

        event.game.getWaitingPlayers().filter { it.uniqueId != player.uniqueId }.forEach {
            it.sendActionBar(
                language(player).format(LOBBY_PLAYER_JOIN, PlainTextComponentSerializer.plainText().serialize(player.displayName()))
            )
        }

        player.inventory.setItem(
            0,
            ItemBuilder(Material.WHITE_BANNER, 1)
                .displayName(language(player).message(LOBBY_TEAM_CHOICE_ITEM_NAME))
                .persistentInfo("teamselectobject", "teamselect", plugin, PersistentDataType.STRING)
                .build()
        )
    }
}