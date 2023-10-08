/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.common.check.name.similarity

import catmoe.fallencrystal.moefilter.check.AbstractCheck
import catmoe.fallencrystal.moefilter.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.collect.EvictingQueue
import com.typesafe.config.ConfigException
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
class SimilarityCheck : AbstractCheck(), Reloadable {
    private var config = LocalConfig.getAntibot().getConfig("name-check.similarity")
    private var maxList = try { config.getInt("max-list") } catch (_: ConfigException) { 1 }
    private var enable = config.getBoolean("enable")
    private var length = config.getInt("length")

    private var validTime = config.getLong("valid-time")
    private var validCache = Caffeine.newBuilder().expireAfterWrite(validTime, TimeUnit.SECONDS).build<String, Boolean>()

    private val banned: MutableCollection<String> = CopyOnWriteArrayList()

    private var debug = LocalConfig.getConfig().getBoolean("debug")

    private var queue = EvictingQueue.create<String>(maxList)

    init { instance = this }

    override fun increase(info: CheckInfo): Boolean {
        if (!enable) return false
        val name = (info as Joining).username.lowercase()
        if (banned.joinToString("|").toRegex().matches(name)) return true
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            if (validCache.getIfPresent(it) == true) {
                if (it == name) { return false } // 防止重新连接的玩家被误判
                val ratio = FuzzySearch.weightedRatio(it, name)
                if (debug) { MessageUtil.logInfo("[MoeFilter] [AntiBot] [SimilarityCheck] Fuzzy searching $it for $name ($ratio length)") }
                if (ratio >= length) { banned.add(name.substring(length)); return true }
            } else { queue.remove(it) }
        }
        queue.add(name)
        validCache.put(name, true)
        return false
    }

    override fun reload() {
        config = LocalConfig.getAntibot().getConfig("name-check.similarity")
        maxList = try { config.getInt("max-list") } catch (_: ConfigException) { 1 }
        enable = config.getBoolean("enable")
        length = config.getInt("length")
        this.queue.clear()
        this.queue = EvictingQueue.create(maxList)
        this.debug = LocalConfig.getConfig().getBoolean("debug")
        this.validTime = config.getLong("valid-time")
        this.validCache = Caffeine.newBuilder().expireAfterWrite(validTime, TimeUnit.SECONDS).build()
    }

    companion object {
        lateinit var instance: SimilarityCheck
            private set
    }
}