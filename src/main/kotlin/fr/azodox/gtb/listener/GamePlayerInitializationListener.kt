package fr.azodox.gtb.listener

import fr.azodox.gtb.event.GamePlayerInitializationEvent
import fr.azodox.gtb.lang.LanguageCore
import fr.azodox.gtb.util.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
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
                MiniMessage.miniMessage()
                    .deserialize(
                        LanguageCore.languages["fr-fr"]!!
                            .getMessage("lobby.player.join")
                            .format(PlainTextComponentSerializer.plainText().serialize(it.displayName()))
                    )
            )
        }

        player.inventory.setItem(
            0,
            ItemBuilder(Material.WHITE_BANNER, 1)
                .displayName(Component.text("Choisir une équipe").color(NamedTextColor.YELLOW))
                .build()
        )
    }
}