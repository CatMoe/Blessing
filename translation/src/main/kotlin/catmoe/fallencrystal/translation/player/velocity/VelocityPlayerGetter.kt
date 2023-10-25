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

package catmoe.fallencrystal.translation.player.velocity

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlayerGetter
import catmoe.fallencrystal.translation.player.TranslatePlayer
import java.util.*

@Platform(ProxyPlatform.VELOCITY)
class VelocityPlayerGetter : PlayerGetter {

    val platform = TranslationLoader.instance.loader.platform

    private fun checkPlatform(): Boolean {
        return platform == ProxyPlatform.VELOCITY
    }

    override fun getPlayer(uuid: UUID): TranslatePlayer? {
        if (!checkPlatform()) return null
        val proxyServer = getProxyServer() as com.velocitypowered.api.proxy.ProxyServer
        return try { TranslatePlayer(VelocityPlayer(proxyServer.getPlayer(uuid).get())) } catch (_: NullPointerException) { null }
    }

    override fun getPlayer(name: String): TranslatePlayer? {
        if (!checkPlatform()) return null
        val proxyServer = getProxyServer() as com.velocitypowered.api.proxy.ProxyServer
        return try { TranslatePlayer(VelocityPlayer(proxyServer.getPlayer(name).get())) } catch (_: NullPointerException) { null }
    }

    private fun getProxyServer(): Any {
        return TranslationLoader.instance.loader.getProxyServer().obj
    }

    override fun getPlayers(): MutableCollection<TranslatePlayer> {
        if (!checkPlatform()) return mutableListOf()
        val proxyServer = getProxyServer() as com.velocitypowered.api.proxy.ProxyServer
        val a: MutableCollection<TranslatePlayer> = ArrayList()
        try {
            proxyServer.allPlayers.forEach { a.add(TranslatePlayer((VelocityPlayer(it)))) }
        } catch (_: Exception) {}
        return a
    }
}