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

package net.miaomoe.blessing.config.type;

import lombok.Getter;
import net.miaomoe.blessing.config.reader.ConfigReader;
import net.miaomoe.blessing.config.reader.typesafe.TypesafeHoconReader;
import net.miaomoe.blessing.config.reader.typesafe.TypesafeJsonReader;
import net.miaomoe.blessing.config.writer.ConfigWriter;
import net.miaomoe.blessing.config.writer.typesafe.TypesafeHoconWriter;
import net.miaomoe.blessing.config.writer.typesafe.TypesafeJsonWriter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("unused")
@Getter
public enum ConfigType {
    HOCON(TypesafeHoconWriter::getInstance, TypesafeHoconReader::getInstance, "conf"),
    @ApiStatus.Experimental
    JSON(TypesafeJsonWriter::getInstance, TypesafeJsonReader::getInstance, "json"),
    @Deprecated
    UNKNOWN(null, null, "");

    private final Supplier<ConfigWriter> writer;
    private final Supplier<ConfigReader> reader;
    private final String suffix;

    ConfigType(
            final @Nullable Supplier<ConfigWriter> writer,
            final @Nullable Supplier<ConfigReader> reader,
            final @NotNull String suffix
    ) {
        this.writer=writer;
        this.reader=reader;
        if (!System.getProperties().contains("config.ignore-check-default-processor")) {
            try {
                if (writer != null) writer.get();
                if (reader != null) reader.get();
            } catch (final NoClassDefFoundError error) {
                System.out.println("The default writer or reader is invalid because you are not loading the specified package.");
                //noinspection CallToPrintStackTrace
                error.printStackTrace();
            }
        }
        this.suffix=suffix;
    }
}
