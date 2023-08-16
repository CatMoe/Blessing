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

package catmoe.fallencrystal.translation.server

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.ProxyPlatform.BUNGEE
import catmoe.fallencrystal.translation.platform.ProxyPlatform.VELOCITY
import catmoe.fallencrystal.translation.server.bungee.BungeeServerGetter
import catmoe.fallencrystal.translation.server.velocity.VelocityServerGetter
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.CopyOnWriteArrayList

object ServerInstance : ServerGetter {

    private val nameCache = Caffeine.newBuilder().build<String, TranslateServer>()
    private val serverList: MutableCollection<TranslateServer> = CopyOnWriteArrayList()

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Don't execute it with translation not startup.")
    override fun init() {
        when (TranslationLoader.instance.loader.platform) {
            BUNGEE -> BungeeServerGetter().init()
            VELOCITY -> VelocityServerGetter().init()
        }
    }

    override fun getServer(name: String): TranslateServer? {
        val a = nameCache.getIfPresent(name)
        if (a != null) return a
        val b = when (TranslationLoader.instance.loader.platform) {
            BUNGEE -> BungeeServerGetter().getServer(name)
            VELOCITY -> VelocityServerGetter().getServer(name)
        }
        if (b != null) addServer(b)
        return b
    }

    fun addServer(server: TranslateServer) { serverList.add(server); nameCache.put(server.getName(), server) }

    fun getServers(): Collection<TranslateServer> { return serverList }

}