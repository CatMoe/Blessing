package catmoe.fallencrystal.moefilter.common.check.domain_check

import java.util.concurrent.atomic.AtomicBoolean
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.AddressCheck

class DomainCheck : AbstractCheck() {
   override fun increase(info: CheckInfo): Boolean {
     val config = LocalConfig.getConfig()
     val host = (info as AddressCheck).address.hostString
     if (!config.getBoolean("domain-check.enabled")) { return true }
     val allowed = AtomicBoolean(false)
     config.getStringList("domain-check.allow-lists").forEach { if (it.lowercase().equals(host.lowercase())) { allowed.set(true) } }
     return allowed.get()
   }
}
