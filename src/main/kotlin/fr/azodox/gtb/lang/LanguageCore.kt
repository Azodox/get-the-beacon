package fr.azodox.gtb.lang

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fr.azodox.gtb.GetTheBeacon
import fr.azodox.gtb.lang.LanguageCore.Companion.DEFAULT_LANGUAGE
import fr.azodox.gtb.util.FileUtil
import fr.azodox.gtb.util.ItemBuilder.fromJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.*
import java.util.UUID
import kotlin.io.path.inputStream
import kotlin.io.path.name

private val LOCALES_FOLDER_IGNORES = listOf("userdata.json")

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
            FileUtil.copyFilesFromJar(plugin.dataFolder, url.toURI())
            val dataFolderLocalesFolder = File(plugin.dataFolder, "/locales")

            Files.list(dataFolderLocalesFolder.toPath()).forEach { file ->
                if (LOCALES_FOLDER_IGNORES.any { file.name == it })
                    return@forEach

                val language = Json.decodeFromStream<Language>(file.inputStream())
                languages[language.locale] = language
                GetTheBeacon.LOGGER.info("Loaded language : ${language.name} (${language.locale})")
            }
        }
        createDataFile()
        DEFAULT_LANGUAGE = languages[plugin.config.getString("locales.default")] ?: Language("Default", "en-us", mapOf())
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun setLocale(player: UUID, locale: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        var userData = mutableMapOf<String, String>()

        if (userDataFile.length() != 0L) {
            userData = Json.decodeFromStream<MutableMap<String, String>>(userDataFile.inputStream())
        }
        userData[player.toString()] = locale
        userDataFile.writeText(gson.toJson(userData))
    }

    fun initPlayer(player: Player) = initPlayer(player.uniqueId)

    @OptIn(ExperimentalSerializationApi::class)
    fun initPlayer(player: UUID) {
        if (userDataFile.length() != 0L) {
            val userData = Json.decodeFromStream<MutableMap<String, String>>(userDataFile.inputStream())
            if (userData.containsKey(player.toString()))
                return
        }
        setLocale(player, DEFAULT_LANGUAGE.locale)
    }

    fun reload(){
        init()
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

fun language(player: Player): Language = language(player.uniqueId)

fun language(player: UUID): Language {
    val gson = Gson()
    val userData = gson.fromJson(LanguageCore.userDataFile.readText(), MutableMap::class.java)
    return LanguageCore.languages[userData[player]] ?: DEFAULT_LANGUAGE
}