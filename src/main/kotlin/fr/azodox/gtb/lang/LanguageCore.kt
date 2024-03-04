package fr.azodox.gtb.lang

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

class LanguageCore {

    companion object {
        @JvmStatic val languages = mutableMapOf<String, Language>()
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
                        println("Loaded language : ${language.name} (${language.locale})")
                    }
                }
            }
        }
    }
}