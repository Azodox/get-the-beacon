package fr.azodox.gtb.game.team.view

import fr.azodox.gtb.game.team.GameTeam
import fr.azodox.gtb.lang.language
import fr.azodox.gtb.util.ItemBuilder
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.Context
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.minimessage.MiniMessage

class GameTeamChoiceView(private val teams: List<GameTeam>) : View() {

    private lateinit var paginationState: State<Pagination>
    override fun onInit(config: ViewConfigBuilder) {
        config.cancelOnDrag()
        config.cancelOnClick()
        config.cancelOnPickup()
        config.cancelOnDrop()

        config.size(3 * 9)
        config.title("").scheduleUpdate(20L)
        this.paginationState = paginationState(teams.toMutableList()) { ctx, builder, _, team ->
            val itemBuilder = ItemBuilder(team.icon, 1).displayName(team.displayName)
            if (team.contains(ctx.player.uniqueId)) {
                itemBuilder.lore(
                    language(ctx.player)
                        .message("lobby.team.choice.lore.already-in")
                        .replaceText {
                            it.match("%team%").replacement(team.name)
                            it.match("%size%").replacement(team.size.toString())
                        }
                )
            } else {
                itemBuilder.lore(
                    language(ctx.player)
                        .message("lobby.team.choice.lore.not-joined")
                        .replaceText {
                            it.match("%team%").replacement(team.name)
                            it.match("%size%").replacement(team.size.toString())
                        }
                )
            }

            builder.withItem(
                itemBuilder.build()
            ).onClick { context ->
                val player = context.player
                if (!team.contains(player.uniqueId)) {
                    team.join(player.uniqueId)

                    player.sendMessage(
                        language(player)
                            .message("lobby.team.choice.joined")
                            .replaceText {
                                it.match("%team%").replacement(team.name)
                            }
                    )
                } else {
                    team.leave(player.uniqueId)

                    player.sendMessage(
                        language(player)
                            .message("lobby.team.choice.left")
                            .replaceText {
                                it.match("%team%").replacement(team.name)
                            }
                    )
                }
                context.closeForPlayer()
            }
        }
        config.scheduleUpdate(20L)
    }

    override fun onUpdate(update: Context) {
        val player = update.player
        update.updateTitleForPlayer(
            MiniMessage.miniMessage().serialize(language(player).message("lobby.team.choice.title")), player
        )
    }
}