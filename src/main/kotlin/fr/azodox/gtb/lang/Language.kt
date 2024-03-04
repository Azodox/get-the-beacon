package fr.azodox.gtb.lang

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

@Serializable
data class Language(
    val name: String,
    val locale: String,
    private val messages: Map<String, String>
) {

    /**
     * Get the message with the given key
     * @param key The key of the message
     * @return The message with the given key or "Message not found" if the key is not found
     */
    fun getMessage(key: String): String {
        return messages[key] ?: "Message not found"
    }

    /**
     * Get the message with the given key
     * @param key The key of the message
     * @return The message with the given key deserialized as a MiniMessage using [MiniMessage.deserialize]
     */
    fun message(key: String): Component {
        return MiniMessage.miniMessage().deserialize(getMessage(key))
    }

    /**
     * Get the message with the given key and format it with the given args
     * @param key The key of the message
     * @param args The arguments to format the message with
     * @return The formatted message deserialized as a MiniMessage using [MiniMessage.deserialize]
     */
    fun format(key: String, vararg args: String): Component {
        return MiniMessage.miniMessage().deserialize(getMessage(key).format(*args))
    }
}
