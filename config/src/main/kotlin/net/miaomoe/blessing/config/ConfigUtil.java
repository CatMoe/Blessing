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

package net.miaomoe.blessing.config;

import net.miaomoe.blessing.config.parser.ConfigParser;
import net.miaomoe.blessing.config.parser.DefaultConfigParserKt;
import net.miaomoe.blessing.config.reader.ConfigReader;
import net.miaomoe.blessing.config.reader.DefaultConfigReaderKt;
import net.miaomoe.blessing.config.writer.ConfigWriter;
import net.miaomoe.blessing.config.writer.DefaultConfigWriterKt;

public class ConfigUtil {
    public static final ConfigParser PARSER = DefaultConfigParserKt.getDefaultConfigParser();
    public static final ConfigReader READER = DefaultConfigReaderKt.getDefaultConfigReader();
    public static final ConfigWriter WRITER = DefaultConfigWriterKt.getDefaultConfigWriter();
}
