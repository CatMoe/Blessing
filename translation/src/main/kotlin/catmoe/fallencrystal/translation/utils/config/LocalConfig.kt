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

@IgnoreInitReload
object LocalConfig : Reloadable {
    private var config = ConfigFactory.parseFile(LoadConfig.instance.configFile)
    private var message = ConfigFactory.parseFile(LoadConfig.instance.messageFile)
    private var proxy = ConfigFactory.parseFile(LoadConfig.instance.proxyFile)
    private var antibot = ConfigFactory.parseFile(LoadConfig.instance.antibotFile)
    private var limbo = ConfigFactory.parseFile(LoadConfig.instance.limboFile)

    fun getConfig(): Config { return config }

    fun getMessage(): Config { return message }

    fun getProxy(): Config { return proxy }

    fun getAntibot(): Config { return antibot }

    fun getLimbo(): Config { return limbo }

    override fun reload() {
        config = ConfigFactory.parseFile(LoadConfig.instance.configFile)
        message = ConfigFactory.parseFile(LoadConfig.instance.messageFile)
        proxy = ConfigFactory.parseFile(LoadConfig.instance.proxyFile)
        antibot = ConfigFactory.parseFile(LoadConfig.instance.antibotFile)
        limbo = ConfigFactory.parseFile(LoadConfig.instance.limboFile)
    }
}