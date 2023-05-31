package catmoe.fallencrystal.moefilter.api.user.displaycache

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.github.benmanes.caffeine.cache.Caffeine
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ProxyServer
import java.util.*

object DisplayCache {
    private val displayCache = Caffeine.newBuilder().build<UUID, Display>()
    fun getDisplay(uuid: UUID): Display {
        if (displayCache.getIfPresent(uuid) == null) { try { updateFromUUID(uuid) } catch (npe: NullPointerException) { return Display(uuid, "", "", "") } }
        return displayCache.getIfPresent(uuid) ?: Display(uuid, "", "", "")
    }
    fun updateDisplayCache(uuid: UUID, display: Display) { if (displayCache.getIfPresent(uuid) != null) { displayCache.invalidate(uuid) }; displayCache.put(uuid, display) }

    fun updateFromUUID(uuid: UUID) {
        ProxyServer.getInstance().scheduler.runAsync(FilterPlugin.getPlugin()) {
            val metaData = LuckPermsProvider.get().userManager.getUser(uuid)!!.cachedData.metaData
            val prefix = metaData.prefix
            val suffix = metaData.suffix
            val player = ProxyServer.getInstance().getPlayer(uuid) ?: return@runAsync
            val name = player.name
            updateDisplayCache(uuid, Display(uuid, prefix ?: "", suffix ?: "", name))
        }
    }
}