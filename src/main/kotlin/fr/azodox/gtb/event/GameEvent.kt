package fr.azodox.gtb.event

import fr.azodox.gtb.game.Game
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class GameEvent(open val game: Game) : Event(false) {

    companion object {
        @JvmField val handlerList: HandlerList = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}