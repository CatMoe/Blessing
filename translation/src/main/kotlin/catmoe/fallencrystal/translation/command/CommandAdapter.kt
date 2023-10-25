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