package fr.azodox.gtb.lang

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.lang.LanguageCore.Companion.DEFAULT_LANGUAGE
import fr.azodox.gtb.lang.LanguageCore.Companion.languages
import fr.azodox.gtb.lang.LanguageCore.Companion.userDataFile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import java.util.zip.ZipEntry
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

class LanguageCore(private val plugin: JavaPlugin) {

    companion object {
        @JvmStatic val languages = mutableMapOf<String, Language>()
        @JvmStatic lateinit var userDataFile: File

        /**
         * The default language set in the config.yml file
         * If no default language is set, a dummy language will be used, empty and marked as the locale "en-us"
         */
        @JvmStatic lateinit var DEFAULT_LANGUAGE: Language
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun init(){
        val url = this::class.java.getResource("/locales")
        if (url != null) {
            val fs = FileSystems.newFileSystem(url.toURI(), mapOf("create" to "true"))
            val path = fs.getPath("/locales")
            Files.walk(path).forEach {
                if(it.isDirectory()) {
                    Files.list(path).forEach {file ->
                        val language = Json.decodeFromStream<Language>(file.inputStream())
                        languages[language.locale] = language
                        GetTheBeacon.LOGGER.info("Loaded language : ${language.name} (${language.locale})")
                    }
                }
            }
        }
        createDataFile()
        DEFAULT_LANGUAGE = languages[plugin.config.getString("locales.default")] ?: Language("Default", "en-us", mapOf())
    }

    fun setLocale(player: UUID, locale: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        var userData = mutableMapOf<UUID, String>()

        if (userDataFile.length() != 0L) {
            userData = gson.fromJson(userDataFile.readText(), MutableMap::class.java) as MutableMap<UUID, String>
        }

        userData[player] = locale
        userDataFile.writeText(gson.toJson(userData))
    }

    private fun createDataFile(){
        val dataFolder = plugin.dataFolder
        val userDataFolder = File(dataFolder, "locales")
        val userData = File(userDataFolder, "userdata.json")
        if (!userData.exists()) {
            userDataFolder.mkdirs()
            userData.createNewFile()
            userDataFile = userData
        }
    }
}

inline fun language(player: Player): Language = language(player.uniqueId)

inline fun language(player: UUID): Language {
    val gson = Gson()
    val userData = gson.fromJson(LanguageCore.userDataFile.readText(), MutableMap::class.java)
    return LanguageCore.languages[userData[player]] ?: DEFAULT_LANGUAGE
}