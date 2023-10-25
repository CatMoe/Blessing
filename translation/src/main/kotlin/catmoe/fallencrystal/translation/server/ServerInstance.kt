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