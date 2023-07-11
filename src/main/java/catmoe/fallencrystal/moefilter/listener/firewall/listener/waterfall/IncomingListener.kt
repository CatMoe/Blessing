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

package catmoe.fallencrystal.moefilter.listener.firewall.listener.waterfall

import catmoe.fallencrystal.moefilter.listener.main.MainListener
import net.md_5.bungee.api.event.PlayerHandshakeEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class IncomingListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onIncomingConnect(event: io.github.waterfallmc.waterfall.event.ConnectionInitEvent) { event.isCancelled = MainListener.initConnection(event.remoteSocketAddress) }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onHandshake(event: PlayerHandshakeEvent) { MainListener.onHandshake(event.handshake, event.connection) }
}