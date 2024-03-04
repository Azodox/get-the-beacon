package fr.azodox.gtb.test.lang

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val name: String,
    val locale: String,
    val messages: Map<String, String>
) {

    fun getMessage(key: String): String {
        return messages[key] ?: "Message not found"
    }
}
