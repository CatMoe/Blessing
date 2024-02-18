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

package net.miaomoe.blessing.config.getter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.HoldingGenericParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

@ToString
@AllArgsConstructor
public class FieldConfigValueGetter implements ConfigValueGetter {

    @NotNull private final AbstractConfig config;
    @Getter @NotNull private final Field field;

    @NotNull
    @Override
    public AbstractConfig getConfig() {
        return config;
    }

    @Override
    public @Nullable Object getValue() {
        try {
            return this.getField().get(this.getConfig());
        } catch (final Exception exception) {
            throw new ConfigGetException("Failed get value from " + getConfig() + " (on method " + getField().getName() + ")", exception);
        }
    }

    @Override
    public @NotNull Class<?> getHoldingGenericType() {
        return HoldingGenericParser.getSingleGenericType(field.getGenericType());
    }
}
