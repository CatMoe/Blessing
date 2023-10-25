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

package catmoe.fallencrystal.translation.utils.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

@Suppress("unused")
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