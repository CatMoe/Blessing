/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.bungee.config;

import net.miaomoe.blessing.bungee.BlessingBungee;
import net.miaomoe.blessing.config.annotation.Comment;
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.exception.ParsedFailedException;
import net.miaomoe.blessing.config.type.ConfigType;
import net.miaomoe.blessing.config.util.SimpleConfigUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlessingBungeeConfig extends AbstractConfig {

    public BlessingBungeeConfig(@NotNull final BlessingBungee plugin) {
        this.version=plugin.getDescription().getVersion();
    }

    public void reload(@NotNull final BlessingBungee plugin) throws ParsedFailedException {
        final File folder = plugin.getDataFolder();
        SimpleConfigUtil.saveAndRead(folder, "config", this, ConfigType.HOCON);
        final Logger logger = plugin.getLogger();
        if (!this.version.equals(plugin.getDescription().getVersion())) {
            final String[] logs = {
                    "The existing config is inconsistent with the plugin version.",
                    "Plugin will attempt to automatically be compatible with existing config.",
                    "Please back up and delete the original configuration file as soon as possible.",
                    "and reload the plugin to generate a new default configuration.",
                    "",
                    "Issues caused by continued use of outdated profiles will not be supported."
            };
            for (String log : logs) logger.log(Level.WARNING, log);
        }
    }

    @ConfigValue
    @Comment(description = "Plugin version. (DO NOT EDIT THIS!)")
    public String version;

    @ConfigValue
    @Comment(description = "Enable debug log & feature.")
    @SuppressWarnings("unused")
    public boolean debug;
}
