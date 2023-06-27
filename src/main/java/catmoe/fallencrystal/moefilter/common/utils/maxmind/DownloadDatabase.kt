package catmoe.fallencrystal.moefilter.common.utils.maxmind

import catmoe.fallencrystal.moefilter.common.utils.maxmind.exception.InvalidMaxmindKeyException
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class DownloadDatabase(folder: File, license: String) {

    private val invalidKeyWarn = "[MoeFilter] [GeoIP] $license is not a valid key."

    init {
        if (license.length != 40) {
            try { throw InvalidMaxmindKeyException("key $license is not a valid key.") } catch (exception: InvalidMaxmindKeyException) { MessageUtil.logWarnRaw(invalidKeyWarn); exception.printStackTrace() }
        }
        if (!folder.exists()) { folder.mkdirs(); download(); extract() }
    }

    private val url = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key=$license&suffix=tar.gz"
    private val downloadPath = Paths.get("${folder.absolutePath}/geolite")
    private val extractPath = Paths.get("${downloadPath.toAbsolutePath()}/geolite/extracted")

    private fun download() {
        try { Files.createDirectories(downloadPath) } catch (exception: IOException) { exception.printStackTrace() } catch (_: Exception) {} 
        try {
            val tarFile = downloadPath.resolve("GeoLite2-Country.tar.gz").toFile()
            BufferedInputStream(URL(url).openStream()).use { bufferedInputStream ->
                BufferedOutputStream(FileOutputStream(tarFile)).use { bufferedOutputStream -> bufferedInputStream.copyTo(bufferedOutputStream)  }
            }
        } catch (exception: Exception) { MessageUtil.logError("[MoeFilter] [GeoIP] Failed to download maxmind database."); exception.printStackTrace(); return }
        MessageUtil.logInfo("[MoeFilter] [GeoIP] Download completed.")
    }

    private fun extract() {
        try {
            TarArchiveInputStream(GzipCompressorInputStream(Files.newInputStream(extractPath))).use { tarArchiveInputStream ->
                var entry: TarArchiveEntry? = tarArchiveInputStream.nextTarEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".mmdb")) {
                        val extractFile = extractPath.resolve(entry.name).toFile()
                        BufferedOutputStream(FileOutputStream(extractFile)).use { bufferedOutputStream ->
                            tarArchiveInputStream.copyTo(bufferedOutputStream)
                        }
                        Files.move(extractFile.toPath(), Paths.get("${downloadPath.toAbsolutePath()}/GeoLite2-Country.mmdb"))
                        MessageUtil.logInfo("[MoeFilter] [GeoIP] Successfully extracted.")
                    }
                    entry = tarArchiveInputStream.nextTarEntry
                }
            }
        } catch (exception: Exception) { MessageUtil.logError("[MoeFilter] [GeoIP] Critical error occurred when extracting .mmdb files."); exception.printStackTrace() }
    }

}