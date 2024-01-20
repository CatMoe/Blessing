/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing

import net.md_5.bungee.api.plugin.Plugin
import net.miaomoe.blessing.config.BlessingBungeeConfig
import net.miaomoe.blessing.util.ReplacedChannelInitializer

class BlessingBungee : Plugin() {

    init {
        instance=this
        config= BlessingBungeeConfig(this)
    }

    override fun onLoad() {
        config.reload(this)
    }


    override fun onEnable() {
        if (config.debug) {
            ReplacedChannelInitializer.inject()
            config.fallback.debugLogger=this.logger
        }
    }

    companion object {
        lateinit var instance: BlessingBungee
            private set
        lateinit var config: BlessingBungeeConfig
            private set
    }

}