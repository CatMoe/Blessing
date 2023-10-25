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

package catmoe.fallencrystal.translation.player

import catmoe.fallencrystal.translation.executor.CommandExecutor
import catmoe.fallencrystal.translation.server.PlatformServer
import catmoe.fallencrystal.translation.server.TranslateServer
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

interface PlatformPlayer : CommandExecutor {

    fun getAddress(): SocketAddress

    fun virtualHost(): InetSocketAddress?

    fun getBrand(): String

    fun getVersion(): Version

    fun getUniqueId(): UUID

    fun isOnlineMode(): Boolean

    fun isOnline(): Boolean

    fun disconnect()

    fun disconnect(reason: Component)

    fun send(server: PlatformServer)

    fun sendActionbar(component: Component)

    fun channel(): Channel

    fun getServer(): TranslateServer

}