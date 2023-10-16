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

package catmoe.fallencrystal.translation.command

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.command.bungee.BungeeCommandAdapter
import catmoe.fallencrystal.translation.command.velocity.VelocityCommandAdapter
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import com.github.benmanes.caffeine.cache.Caffeine
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
object CommandAdapter {

    private val platform = TranslationLoader.instance.loader.platform
    val adapter: KClass<out ICommandAdapter> = when (platform) {
        ProxyPlatform.BUNGEE -> BungeeCommandAdapter::class
        ProxyPlatform.VELOCITY -> VelocityCommandAdapter::class
    }

    val list = mutableListOf<TranslationCommand>()
    val adapters = Caffeine.newBuilder().build<TranslationCommand, ICommandAdapter>()

    fun register(command: TranslationCommand): ICommandAdapter {
        val adapter = when (platform) {
            ProxyPlatform.BUNGEE -> BungeeCommandAdapter(command)
            ProxyPlatform.VELOCITY -> VelocityCommandAdapter(command)
        }
        adapter.register()
        list.add(command)
        adapters.put(command, adapter)
        return adapter
    }

    fun unregister(command: TranslationCommand) {
        val adapter = this.adapters.getIfPresent(command) ?: return
        adapter.unregister()
        list.remove(command)
        adapters.invalidate(command)
    }

}