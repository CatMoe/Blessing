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

package catmoe.fallencrystal.moefilter.common.config.util

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

class CreateConfig(val file: File) {
    private var defaultConfig=""
    private var configName="config.conf"

    private var configFile: File? = null

    private var configObject: Config? = null

    fun setDefaultConfig(defaultConfig: String) { this.defaultConfig=defaultConfig }
    fun setDefaultConfig(defaultConfig: List<String>) { this.defaultConfig=defaultConfig.joinToString("\n") }
    fun setConfigName(configName: String) { this.configName=configName }
    fun setConfigFile(configFile: File) { this.configFile=configFile }

    fun onLoad() {
        try {
            if (!file.exists()) { file.mkdirs() }
            val file = configFile ?: File(file.absolutePath, configName)
            if (!file.exists()) {
                file.createNewFile()
                if (defaultConfig.isNotEmpty()) file.writeText(defaultConfig)
            }
            configObject=ConfigFactory.parseFile(file)
        } catch (exception: Exception) { exception.printStackTrace() }
    }

    fun getConfig(): Config? { return configObject }
}