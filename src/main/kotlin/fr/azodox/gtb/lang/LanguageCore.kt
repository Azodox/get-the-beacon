package fr.azodox.gtb.lang

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isDirectory

class LanguageCore {

    companion object {
        @JvmStatic val languages = mutableMapOf<String, Language>()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun init(){
        val url = this::class.java.getResource("locales")
        if (url != null) {
            val path = Paths.get(url.toURI())
            Files.walk(path).forEach {
                if(it.isDirectory() && it.toFile().listFiles() != null) {
                    it.toFile().listFiles()!!.forEach {file ->
                        val language = Json.decodeFromStream<Language>(file.inputStream())
                        languages[language.locale] = language
                        println("Loaded language : ${language.name} (${language.locale})")
                    }
                }
            }
        }
    }
}