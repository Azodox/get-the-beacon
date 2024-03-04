package fr.azodox.gtb.test.lang

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

class LanguageCore {

    private val languages = mutableMapOf<String, Language>()

    @OptIn(ExperimentalSerializationApi::class)
    fun init(){
        val url = this::class.java.getResource("/locales")
        if (url != null) {
            val fs = FileSystems.newFileSystem(url.toURI(), mapOf("create" to "true"))
            val path = fs.getPath("/locales")
            Files.walk(path).forEach {
                if(it.isDirectory()) {
                    Files.list(path).forEach {file ->
                        val language = Json.decodeFromStream<fr.azodox.gtb.lang.Language>(file.inputStream())
                        fr.azodox.gtb.lang.LanguageCore.languages[language.locale] = language
                        println("Loaded language : ${language.name} (${language.locale})")
                    }
                }
            }
        }
    }

    fun getLanguage(locale: String): Language? {
        return languages[locale]
    }
}

fun main() {
    val languageCore = LanguageCore()
    languageCore.init()
    languageCore.getLanguage("fr-fr")?.getMessage("start.notenoughplayers")?.let { println(it) }
}