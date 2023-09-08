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

package catmoe.fallencrystal.translation.player

import catmoe.fallencrystal.translation.server.PlatformServer
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

// 由于类的一些问题 我们需要用一个用于兼容的类来包裹那些
// 尽管如此 也可以通过 is 来判断是VelocityPlayer还是BungeePlayer并获取它们原本的属性.
@Suppress("MemberVisibilityCanBePrivate")
class TranslatePlayer(val upstream: PlatformPlayer): PlatformPlayer {
    override fun getAddress(): SocketAddress {
        return upstream.getAddress()
    }

    override fun virtualHost(): InetSocketAddress? {
        return upstream.virtualHost()
    }

    override fun getBrand(): String {
        return upstream.getBrand()
    }

    override fun getVersion(): Version {
        return upstream.getVersion()
    }

    override fun getName(): String {
        return upstream.getName()
    }

    override fun sendMessage(component: Component) {
        upstream.sendMessage(component)
    }

    override fun hasPermission(permission: String): Boolean {
        return upstream.hasPermission(permission)
    }

    override fun getUniqueId(): UUID {
        return upstream.getUniqueId()
    }

    override fun isOnlineMode(): Boolean {
        return upstream.isOnlineMode()
    }

    override fun isOnline(): Boolean {
        return upstream.isOnline()
    }

    override fun disconnect() {
        upstream.disconnect()
    }

    override fun disconnect(reason: Component) {
        upstream.disconnect(reason)
    }

    override fun send(server: PlatformServer) {
        upstream.send(server)
    }

    override fun sendActionbar(component: Component) {
        upstream.sendActionbar(component)
    }

    override fun channel(): Channel {
        return upstream.channel()
    }
}