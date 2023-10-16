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

package catmoe.fallencrystal.translation.player.velocity

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlayerGetter
import catmoe.fallencrystal.translation.player.TranslatePlayer
import java.util.*

@Platform(ProxyPlatform.VELOCITY)
class VelocityPlayerGetter : PlayerGetter {
    override fun getPlayer(uuid: UUID): TranslatePlayer? {
        if (TranslationLoader.instance.loader.platform != ProxyPlatform.VELOCITY) throw IllegalArgumentException("Wrong proxy type")
        val proxyServer = getProxyServer() as com.velocitypowered.api.proxy.ProxyServer
        return try { TranslatePlayer(VelocityPlayer(proxyServer.getPlayer(uuid).get())) } catch (_: NullPointerException) { null }
    }

    override fun getPlayer(name: String): TranslatePlayer? {
        if (TranslationLoader.instance.loader.platform != ProxyPlatform.VELOCITY) throw IllegalArgumentException("Wrong proxy type")
        val proxyServer = getProxyServer() as com.velocitypowered.api.proxy.ProxyServer
        return try { TranslatePlayer(VelocityPlayer(proxyServer.getPlayer(name).get())) } catch (_: NullPointerException) { null }
    }

    private fun getProxyServer(): Any {
        if (TranslationLoader.instance.loader.platform != ProxyPlatform.VELOCITY) throw IllegalArgumentException("Wrong proxy type")
        return TranslationLoader.instance.loader.getProxyServer().obj
    }

    override fun getPlayers(): MutableCollection<TranslatePlayer> {
        if (TranslationLoader.instance.loader.platform != ProxyPlatform.VELOCITY) throw IllegalArgumentException("Wrong proxy type")
        val proxyServer = getProxyServer() as com.velocitypowered.api.proxy.ProxyServer
        val a: MutableCollection<TranslatePlayer> = ArrayList()
        try {
            proxyServer.allPlayers.forEach { a.add(TranslatePlayer((VelocityPlayer(it)))) }
        } catch (_: Exception) {}
        return a
    }
}