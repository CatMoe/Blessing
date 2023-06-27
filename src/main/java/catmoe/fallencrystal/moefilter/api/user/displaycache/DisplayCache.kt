package catmoe.fallencrystal.moefilter.api.user.displaycache

import catmoe.fallencrystal.moefilter.MoeFilter
import com.github.benmanes.caffeine.cache.Caffeine
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ProxyServer
import java.util.*

object DisplayCache {
    private val displayCache = Caffeine.newBuilder().build<UUID, Display>()
    fun getDisplay(uuid: UUID): Display {
        if (displayCache.getIfPresent(uuid) == null) { try { updateFromUUID(uuid) } catch (npe: NullPointerException) { return Display(uuid, "", "") } }
        return displayCache.getIfPresent(uuid) ?: Display(uuid, "", "")
    }
    fun updateDisplayCache(uuid: UUID, display: Display) { if (displayCache.getIfPresent(uuid) != null) { displayCache.invalidate(uuid) }; displayCache.put(uuid, display) }

    private fun updateFromUUID(uuid: UUID) {
        ProxyServer.getInstance().scheduler.runAsync(MoeFilter.instance) {
            val metaData = LuckPermsProvider.get().userManager.getUser(uuid)!!.cachedData.metaData
            val prefix = metaData.prefix
            val suffix = metaData.suffix
            updateDisplayCache(uuid, Display(uuid, prefix ?: "", suffix ?: ""))
        }
    }
}