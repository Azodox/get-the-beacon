package fr.azodox.gtb.lang

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
data class Language(
    val name: String,
    val locale: String,
    private val messages: Map<String, String>
) {

    fun getMessage(key: String): String {
        return messages[key] ?: "Message not found"
    }

    fun message(key: String): Component {
        return Component.text(getMessage(key))
    }
}
