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

package catmoe.fallencrystal.translation.player.bungee

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlayerGetter
import catmoe.fallencrystal.translation.player.TranslatePlayer
import java.util.*

@Platform(ProxyPlatform.BUNGEE)
class BungeePlayerGetter : PlayerGetter {

    val platform = TranslationLoader.instance.loader.platform

    private fun checkPlatform(): Boolean {
        return platform == ProxyPlatform.BUNGEE
    }

    override fun getPlayer(uuid: UUID): TranslatePlayer? {
        if (!checkPlatform()) return null
        return try { TranslatePlayer(BungeePlayer(net.md_5.bungee.api.ProxyServer.getInstance().getPlayer(uuid))) } catch (_: NullPointerException) { null }
    }

    override fun getPlayer(name: String): TranslatePlayer? {
        if (!checkPlatform()) return null
        return try { TranslatePlayer(BungeePlayer(net.md_5.bungee.api.ProxyServer.getInstance().getPlayer(name))) } catch (_: NullPointerException) { null }
    }

    override fun getPlayers(): MutableCollection<TranslatePlayer> {
        if (!checkPlatform()) return mutableListOf()
        val a: MutableCollection<TranslatePlayer> = ArrayList()
        try {
            net.md_5.bungee.api.ProxyServer.getInstance().players.forEach { a.add(TranslatePlayer(BungeePlayer(it))) }
        } catch (_: Exception) {}
        return a
    }
}