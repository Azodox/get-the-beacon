package fr.azodox.gtb.util

import fr.azodox.gtb.GetTheBeacon
import java.io.File
import java.net.URI
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.toPath

object FileUtil {
    /**
     * Copy files from the plugin's jar file to the destination folder passed in parameter.
     * Directories and subdirectories are supported.
     * @param destinationFolder The folder to copy the files to
     * @param uris The URIs of the files to copy
     */
    fun copyFilesFromJar(destinationFolder: File, vararg uris: URI) {
        uris.forEach { uri ->
            try {
                FileSystems.newFileSystem(uri, mapOf("create" to "true"))
            } catch (_: FileSystemAlreadyExistsException) {
                FileSystems.getFileSystem(uri)
            }
            val path = uri.toPath()

            Files.walk(path).forEach { sourcePath ->
                val targetPath = Paths.get(destinationFolder.absolutePath, sourcePath.toString())

                /*
                Check if the file already exists in order to avoid FileAlreadyExistsException
                 */
                if (Files.exists(targetPath))
                    return@forEach

                Files.copy(sourcePath, targetPath)
                GetTheBeacon.LOGGER.info("Copied ${sourcePath.fileName.name} to data folder!")
            }
        }
    }
}
