package fr.azodox.gtb.listener.game.player

import fr.azodox.gtb.event.game.player.GamePlayerInitializationEvent
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.ItemBuilder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GamePlayerInitializationListener : Listener {

    @EventHandler
    fun onGamePlayerInitialization(event: GamePlayerInitializationEvent) {
        val player = event.player
        event.game.getWaitingPlayers().filter { it.uniqueId != player.uniqueId }.forEach {
            it.sendActionBar(
                language(player)
                    .format("lobby.player.join", PlainTextComponentSerializer.plainText().serialize(player.displayName()))
            )
        }

        player.inventory.setItem(
            0,
            ItemBuilder(Material.WHITE_BANNER, 1)
                .displayName(
                    language(player).message("lobby.team.choice.item.name")
                )
                .build()
        )
    }
}