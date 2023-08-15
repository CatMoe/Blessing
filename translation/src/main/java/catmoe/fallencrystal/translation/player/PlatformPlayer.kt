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

import catmoe.fallencrystal.translation.utils.version.Version
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

interface PlatformPlayer {

    fun getAddress(): SocketAddress

    fun virtualHost(): InetSocketAddress?

    fun getBrand(): String

    fun getVersion(): Version

    fun getName(): String

    fun getUUID(): UUID

    fun isOnlineMode(): Boolean

    fun isOnline(): Boolean

    fun disconnect()

    fun disconnect(reason: Component)

}