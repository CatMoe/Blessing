package catmoe.fallencrystal.moefilter.common.check.domain_check

import java.util.concurrent.atomic.AtomicBoolean
import com.github.benmanes.caffeine.cache.Caffeine
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.AddressCheck

object DomainCheck : AbstractCheck() {

   private val cache = Caffeine.newBuilder().build<String, Boolean>()
   private var enable = false
   
   override fun increase(info: CheckInfo): Boolean {
     if (!enable) { return true }
     return cache.getIfPresent((info as AddressCheck).address.hostString.lowercase()) != null
   }

   fun init() {
      cache.invalidateAll()
      val config = LocalConfig.getConfig().getConfig("domain-check")
      enable = config.getBoolean("enabled")
      config.getStringList("domain-check.allow-lists").forEach { cache.put(it.lowercase(), true) }
   }
}
