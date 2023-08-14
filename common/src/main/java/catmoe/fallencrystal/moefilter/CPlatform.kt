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

package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.common.config.LoadConfig
import catmoe.fallencrystal.moefilter.platform.MoeProxyServer
import catmoe.fallencrystal.moefilter.platform.Platform
import catmoe.fallencrystal.moefilter.platform.PlatformLoader
import catmoe.fallencrystal.moefilter.platform.SimpleLogger
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
class CPlatform(val loader: PlatformLoader) : PlatformLoader {

    init { instance=this }

    val platform = this.loader::class.java.getAnnotation(Platform::class.java).platform

    override fun getPluginFolder(): File { return loader.getPluginFolder() }
    override fun getPluginInstance(): PlatformLoader { return loader.getPluginInstance() }
    override fun getPlatformLogger(): SimpleLogger { return loader.getPlatformLogger() }
    override fun getProxyServer(): MoeProxyServer { return loader.getProxyServer() }

    override fun readyLoad() { LoadConfig().loadConfig() }

    override fun whenLoad() {}

    override fun whenUnload() {

    }

    override fun pluginVersion(): String { return loader.pluginVersion() }

    companion object {
        lateinit var instance: CPlatform
            private set
    }
}