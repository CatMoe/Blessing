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