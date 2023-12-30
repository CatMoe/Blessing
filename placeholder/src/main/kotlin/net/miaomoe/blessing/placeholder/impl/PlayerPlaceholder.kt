/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.placeholder.impl

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.miaomoe.blessing.placeholder.PlaceholderExpansion
import java.net.InetSocketAddress

object PlayerPlaceholder : PlaceholderExpansion {
    override fun name() = "Player Placeholder"
    override fun identifier() = "player"
    override fun request(target: CommandSender?, input: String): String? {
        require(target != null) { "Target cannot be null!" }
        val split = input.split("_")
        val player = target as? ProxiedPlayer
        return when (split[0]) {
            "name" -> target.name
            "server" -> player?.server?.info?.name
            "address" -> player?.socketAddress.let { (it as? InetSocketAddress)?.hostString }
            "from" -> player?.pendingConnection?.virtualHost?.let { "${it.hostString}:${it.port}" }
            "version" -> player?.pendingConnection?.version?.toString()
            "online" -> player?.isConnected?.toString()
            else -> null
        }
    }
}