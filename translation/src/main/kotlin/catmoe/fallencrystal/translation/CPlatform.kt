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

package catmoe.fallencrystal.translation

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.PlatformLoader
import catmoe.fallencrystal.translation.utils.config.LoadConfig

@Suppress("MemberVisibilityCanBePrivate")
class CPlatform(val loader: PlatformLoader) : PlatformLoader {

    init { instance =this }

    val platform = this.loader::class.java.getAnnotation(Platform::class.java).platform

    val translationLoader = TranslationLoader(this)

    override fun getPluginFolder() = loader.getPluginFolder()
    override fun getPluginInstance() = loader.getPluginInstance()
    override fun getPlatformLogger() = loader.getPlatformLogger()
    override fun getProxyServer() = loader.getProxyServer()

    override fun readyLoad() {
        LoadConfig().loadConfig()
    }

    override fun whenLoad() {
        translationLoader.load()
    }

    override fun whenUnload() {
        // Do not need that.
    }

    override fun pluginVersion() = loader.pluginVersion()

    companion object {
        lateinit var instance: CPlatform
            private set
    }
}