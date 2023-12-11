/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.common.geoip

import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import net.md_5.bungee.api.scheduler.ScheduledTask
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.nio.channels.Channels
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream

/*
Borrowed from :
https://github.com/awumii/EpicGuard/blob/master/core/src/main/java/me/xneox/epicguard/core/manager/GeoManager.java
https://github.com/awumii/EpicGuard/blob/master/core/src/main/java/me/xneox/epicguard/core/util/FileUtils.java
https://github.com/awumii/EpicGuard/blob/master/core/src/main/java/me/xneox/epicguard/core/util/URLUtils.java
 */
@Suppress("SpellCheckingInspection")
class DownloadDatabase(folder: File) {

    private val currentTime = System.currentTimeMillis()
    private val conf = LocalConfig.getProxy().getConfig("country")
    private val parent = File(folder, "/geoip")
    private val scheduler = Scheduler.getDefault()

    private var proxy: Proxy? = null

    init {
        if (!parent.exists()) { parent.mkdirs() }
        val proxyConfig = LocalConfig.getProxy().getConfig("proxies-config")
        val proxyType = try { Proxy.Type.valueOf(proxyConfig.getAnyRef("mode").toString()) } catch (ex: Exception) { Proxy.Type.DIRECT }
        if (proxyType != Proxy.Type.DIRECT) {
            this.proxy = Proxy(proxyType, InetSocketAddress(proxyConfig.getString("host"), proxyConfig.getInt("port")))
        }
    }

    private val countryDatabase = File(parent, "GeoLite2-Country.mmdb")
    private val cityDatabase = File(parent, "GeoLite2-City.mmdb")
    private val countryArchive = File(parent, "GeoLite2-Country.tar.gz")
    private val cityArchive = File(parent, "GeoLite2-City.tar.gz")
    private val countryAvailable = AtomicBoolean(false)
    private val cityAvailable = AtomicBoolean(false)
    private val hasError = AtomicBoolean(false)

    private val license = conf.getString("key")
    private val timeout = conf.getInt("time-out")
    private val debug = LocalConfig.getConfig().getBoolean("debug")

    init {
        try {
            update()
            var task: ScheduledTask? = null
            task = scheduler.repeatScheduler(1, 1, TimeUnit.SECONDS) {
                when {
                    (countryAvailable.get() && cityAvailable.get()) -> {
                        GeoIPManager.country = DatabaseReader.Builder(countryDatabase).withCache(CHMCache()).build()
                        GeoIPManager.city = DatabaseReader.Builder(cityDatabase).withCache(CHMCache()).build()
                        GeoIPManager.available.set(true)
                        scheduler.repeatScheduler(1, 1, TimeUnit.DAYS) { update() }
                        scheduler.cancelTask(task!!)
                    }
                    hasError.get() -> { MessageUtil.logWarn("[MoeFilter] [GeoIP] Error detected. Cancelling init task."); scheduler.cancelTask(task!!) }
                    debug -> MessageUtil.logInfo("[MoeFilter] [GeoIP] Waiting download task complete..")
                }
            }
        } catch (ex: IOException) {
            MessageUtil.logError("[MoeFilter] [GeoIP] A critical error occurred when initing database files.")
            if (!hasError.get()) {
                listOf(
                    "Database are dropped. This feature will disable until restart the proxy.",
                    "",
                    "If you confirm that there is no problem with your network/proxy.",
                    "And this problem always occurs. Please enable debug mode to print the error stack trace and report that.",
                    "If you close the proxy while downloading. This can also cause this to occur."
                ).forEach { MessageUtil.logWarn("[MoeFilter] [GeoIP] $it") }
                hasError.set(true)
            }
            countryDatabase.delete()
            cityDatabase.delete()
            GeoIPManager.available.set(false)
            if (LocalConfig.getConfig().getBoolean("debug")) { ex.printStackTrace() }
        }
    }

    private fun update() {
        scheduler.runAsync {
            try { downloadDatabase(countryDatabase, countryArchive, getUrl(countryDatabase)); countryAvailable.set(true) } catch (ex: Exception) { throwError(countryDatabase, countryArchive, ex); countryAvailable.set(false) }
        }
        scheduler.runAsync {
            try { downloadDatabase(cityDatabase, cityArchive, getUrl(cityDatabase)); cityAvailable.set(true) } catch (ex: Exception) { throwError(cityDatabase, cityArchive, ex); cityAvailable.set(false) }
        }
    }

    private fun throwError(db: File, archive: File, throwable: Throwable) {
        val debug = LocalConfig.getConfig().getBoolean("debug")
        MessageUtil.logError("[MoeFilter] [GeoIP] A critical error occurred when initing database files. (${db.name})")
        listOf(
            "Database are dropped. This feature will disable until restart the proxy.",
            "",
            "If you confirm that there is no problem with your network/proxy.",
            "And this problem always occurs. Please enable debug mode to print the error stack trace and report that.",
            "If you close the proxy while downloading. This can also cause this to occur."
        ).forEach { MessageUtil.logWarn("[MoeFilter] [GeoIP] $it") }
        if (debug) { throwable.printStackTrace() }
        db.delete()
        archive.delete()
        hasError.set(true)
    }



    @Suppress("DEPRECATION")
    private fun downloadDatabase(database: File, archive: File, url: String) {
        if (!database.exists() || currentTime - database.lastModified() > TimeUnit.DAYS.toMillis(7)) {
            MessageUtil.logInfo("[MoeFilter] [GeoIP] ${database.name} has a update available. Downloading it...")
            MessageUtil.logInfo("[MoeFilter] [GeoIP] Downloading the database file ${database.name}")
            downloadUtil(archive, url)
            MessageUtil.logInfo("[MoeFilter] [GeoIP] Download completed. Extracting...")
            val tarInput = TarArchiveInputStream(GZIPInputStream(FileInputStream(archive)))
            var entry: TarArchiveEntry? = tarInput.nextTarEntry
            while (entry != null) {
                if (entry.name.endsWith(database.name)) { IOUtils.copy(tarInput, FileOutputStream(database)) }
                entry = tarInput.nextTarEntry
            }
            tarInput.close()
            archive.delete()
            MessageUtil.logInfo("[MoeFilter] [GeoIP] Completed extract task. (${database.name})")
        } else { MessageUtil.logInfo("[MoeFilter] [GeoIP] No update available for database ${database.name}") }
    }

    private fun downloadUtil(file: File, url: String) {
        file.delete()
        file.createNewFile()
        val initUrl = URL(url)
        val connection = if (proxy != null) initUrl.openConnection(proxy!!) else initUrl.openConnection()
        connection.addRequestProperty("User-Agent", "Mozilla/4.0")
        connection.connectTimeout = timeout
        connection.readTimeout = timeout
        val channels = Channels.newChannel(connection.getInputStream())
        val outputStream = FileOutputStream(file)
        outputStream.channel.transferFrom(channels, 0, Long.MAX_VALUE)
    }

    private fun getUrl(db: File): String {
        val download = "https://download.maxmind.com/app/geoip_download"
        return "$download?edition_id=${db.name.replace(".mmdb", "")}&license_key=$license&suffix=tar.gz"
    }


}