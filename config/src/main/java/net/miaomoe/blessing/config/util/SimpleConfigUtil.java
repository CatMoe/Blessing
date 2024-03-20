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

package net.miaomoe.blessing.config.util;

import lombok.experimental.UtilityClass;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.DefaultConfigParser;
import net.miaomoe.blessing.config.parser.exception.ParsedFailedException;
import net.miaomoe.blessing.config.type.ConfigType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@SuppressWarnings("ResultOfMethodCallIgnored")
@UtilityClass
public class SimpleConfigUtil {

    public void saveAndRead(
            final @NotNull File file,
            final @NotNull AbstractConfig config,
            final @NotNull ConfigType type
    ) throws ParsedFailedException {
        try {
            DefaultConfigParser.getInstance().parse(config);
            if (config.getParsedValues().isEmpty()) throw new NullPointerException("Parsed values list is empty!");
            if (file.exists()) {
                final StringBuilder sb = new StringBuilder();
                final String lineSeparator = System.lineSeparator();
                for (final @NotNull String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                    sb.append(line).append(lineSeparator);
                }
                type.getReader().get().read(sb.toString(), config);
            } else {
                file.createNewFile();
                Files.write(file.toPath(), type.getWriter().get().write(config).getBytes(StandardCharsets.UTF_8));
            }
        } catch (final Throwable throwable) {
            throw new ParsedFailedException(throwable);
        }
    }

    public void saveAndRead(
            final @NotNull File folder,
            final @NotNull String name,
            final @NotNull AbstractConfig config,
            final @NotNull ConfigType type
    ) throws ParsedFailedException {
        if (!folder.exists()) folder.mkdirs();
        saveAndRead(new File(folder, name + "." + type.getSuffix()), config, type);
    }

}
