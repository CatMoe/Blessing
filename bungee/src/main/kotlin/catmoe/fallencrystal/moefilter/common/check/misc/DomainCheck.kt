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

package catmoe.fallencrystal.moefilter.common.check.misc

import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Address
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine

class DomainCheck : AbstractCheck(), Reloadable {

   private val cache = Caffeine.newBuilder().build<String, Boolean>()
   private var enable = false
   private var debug = false

   init { instance=this; init() }
   
   override fun increase(info: CheckInfo): Boolean {
      if (!enable) return false
      val address = (info as Address).address
      val host = info.virtualHost!!.hostString.lowercase()
      if (host == address.address.toString().replace("/", "")) return false
      if (debug) { MessageUtil.logInfo("[MoeFilter] [AntiBot] [DomainCheck] ${info.address.address} try to connect from $host") }
      return cache.getIfPresent(host) == null
   }

   override fun reload() {
      this.init()
   }

   fun init(): DomainCheck {
      cache.invalidateAll()
      val config = LocalConfig.getConfig().getConfig("domain-check")
      enable = config.getBoolean("enabled")
      debug = LocalConfig.getConfig().getBoolean("debug")
      config.getStringList("allow-lists").forEach { cache.put(it.lowercase(), true) }
      return this
   }

   companion object {
      lateinit var instance: DomainCheck
          private set
   }
}
