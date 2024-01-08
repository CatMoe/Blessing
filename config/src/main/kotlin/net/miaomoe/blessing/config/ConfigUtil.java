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

import com.typesafe.config.ConfigFactory;
import net.miaomoe.blessing.config.parser.ConfigParser;
import net.miaomoe.blessing.config.parser.DefaultConfigParserKt;
import net.miaomoe.blessing.config.reader.ConfigReader;
import net.miaomoe.blessing.config.reader.DefaultConfigReaderKt;
import net.miaomoe.blessing.config.writer.ConfigWriter;
import net.miaomoe.blessing.config.writer.DefaultConfigWriterKt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {
    public static final ConfigParser PARSER = DefaultConfigParserKt.getDefaultConfigParser();
    public static final ConfigReader READER = DefaultConfigReaderKt.getDefaultConfigReader();
    public static final ConfigWriter WRITER = DefaultConfigWriterKt.getDefaultConfigWriter();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveAndRead(
            @NotNull final File file,
            @NotNull final AbstractConfig config
    ) throws IOException {
        if (config.getParsed().isEmpty()) PARSER.parse(config);
        if (!file.exists()) {
            file.createNewFile();
            WRITER.write(file, config);
        }
        READER.read(ConfigFactory.parseFile(file), config);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveAndRead(
            @NotNull final File folder,
            @NotNull final String name,
            @NotNull final AbstractConfig config
    ) throws IOException {
        if (!folder.exists()) folder.createNewFile();
        saveAndRead(new File(folder, name + ".conf"), config);
    }
}
