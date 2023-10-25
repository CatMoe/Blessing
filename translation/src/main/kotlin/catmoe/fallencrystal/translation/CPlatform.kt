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

package catmoe.fallencrystal.translation

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.PlatformLoader
import catmoe.fallencrystal.translation.utils.config.LoadConfig

@Suppress("MemberVisibilityCanBePrivate")
class CPlatform(val loader: PlatformLoader) : PlatformLoader by loader {

    init { instance =this }

    val platform = this.loader::class.java.getAnnotation(Platform::class.java).platform

    val translationLoader = TranslationLoader(this)

    override fun readyLoad() {
        LoadConfig().loadConfig()
    }

    override fun whenLoad() {
        translationLoader.load()
    }

    override fun whenUnload() {
        // Do not need that.
    }

    companion object {
        lateinit var instance: CPlatform
            private set
    }
}