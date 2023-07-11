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

package catmoe.fallencrystal.moefilter.common.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object LocalConfig {
    private var config = ConfigFactory.parseFile(LoadConfig.instance.getConfigFile())
    private var message = ConfigFactory.parseFile(LoadConfig.instance.getMessage())
    private var proxy = ConfigFactory.parseFile(LoadConfig.instance.getProxy())
    private var antibot: Config = ConfigFactory.parseFile(LoadConfig.instance.getAntibot())

    fun getConfig(): Config { return config }

    fun getMessage(): Config { return message }

    fun getProxy(): Config { return proxy }

    fun getAntibot(): Config { return antibot }

    fun reloadConfig() {
        config = ConfigFactory.parseFile(LoadConfig.instance.getConfigFile())
        message = ConfigFactory.parseFile(LoadConfig.instance.getMessage())
        proxy = ConfigFactory.parseFile(LoadConfig.instance.getProxy())
        antibot = ConfigFactory.parseFile(LoadConfig.instance.getAntibot())
    }
}