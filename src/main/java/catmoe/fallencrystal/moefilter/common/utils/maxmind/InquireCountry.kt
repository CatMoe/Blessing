package catmoe.fallencrystal.moefilter.common.utils.maxmind

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CountryResponse
import java.io.File
import java.net.InetAddress

object InquireCountry {
    private val databaseFile = File("${FilterPlugin.getDataFolder()!!}/geolite/GeoLite2-Country.mmdb")
    private val reader = DatabaseReader.Builder(databaseFile).build()

    private val enabled = try { ObjectConfig.getProxy().getBoolean("country.enabled") } catch (_: Exception) { false }

    fun check(address: InetAddress): CountryResponse? { return if (enabled) { try { reader.country(address) } catch (_: Exception) { null } } else { null } }
}