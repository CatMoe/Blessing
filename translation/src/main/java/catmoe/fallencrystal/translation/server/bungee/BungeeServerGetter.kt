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

package catmoe.fallencrystal.translation.server.bungee

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.server.ServerGetter
import catmoe.fallencrystal.translation.server.ServerInstance
import catmoe.fallencrystal.translation.server.TranslateServer
import net.md_5.bungee.BungeeCord

@Platform(ProxyPlatform.BUNGEE)
class BungeeServerGetter : ServerGetter {
    override fun getServer(name: String): TranslateServer? {
        return try { TranslateServer(BungeeServer(BungeeCord.getInstance().getServerInfo(name))) } catch(_: Exception) { null }
    }

    override fun init() {
        BungeeCord.getInstance().servers.forEach { ServerInstance.addServer(TranslateServer(BungeeServer(it.value))) }
    }
}