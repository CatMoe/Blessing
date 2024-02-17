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

package net.miaomoe.blessing.config.writer.typesafe;

import lombok.Getter;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.writer.ConfigWriter;
import org.jetbrains.annotations.NotNull;

public class TypesafeJsonWriter extends TypesafeWriterAdapter implements ConfigWriter {

    @Getter
    private static final TypesafeJsonWriter instance = new TypesafeJsonWriter();

    private TypesafeJsonWriter() {
        super(true);
    }

    @Override
    public @NotNull String write(@NotNull AbstractConfig config) {
        return this.toString(config);
    }
}
