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

package catmoe.fallencrystal.moefilter.common.check.misc

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.google.common.collect.EvictingQueue
import com.typesafe.config.ConfigException
import me.xdrop.fuzzywuzzy.FuzzySearch

@Suppress("UnstableApiUsage")
class SimilarityCheck : AbstractCheck() {
    private var config = LocalConfig.getAntibot().getConfig("general.similarity")
    private var maxList = try { config.getInt("max-list") } catch (_: ConfigException) { 1 }
    private var enable = config.getBoolean("enable")
    private var length = config.getInt("length")

    private var debug = LocalConfig.getConfig().getBoolean("debug")

    private var queue = EvictingQueue.create<String>(maxList)

    init { instance=this }

    override fun increase(info: CheckInfo): Boolean {
        if (!enable) { return false }
        val name = (info as Joining).username.lowercase()
        queue.forEach {
            val ratio = FuzzySearch.weightedRatio(it, name)
            if (debug) { MessageUtil.logInfo("[MoeFilter] [AntiBot] [SimilarityCheck] Fuzzy searching $it for $name ($ratio length)") }
            if (ratio >= length) { return true }
        }
        queue.add(name)
        return false
    }

    fun reload() {
        config = LocalConfig.getAntibot().getConfig("general.similarity")
        maxList = try { config.getInt("max-list") } catch (_: ConfigException) { 1 }
        enable = config.getBoolean("enable")
        length = config.getInt("length")
        this.queue.clear()
        this.queue = EvictingQueue.create(maxList)
        this.debug = LocalConfig.getConfig().getBoolean("debug")
    }

    companion object {
        lateinit var instance: SimilarityCheck
            private set
    }
}