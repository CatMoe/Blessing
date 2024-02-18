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

package net.miaomoe.blessing.config.setter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.HoldingGenericParser;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

@ToString
@AllArgsConstructor
public class FieldConfigValueSetter implements ConfigValueSetter {

    @NotNull private final AbstractConfig config;
    @Getter @NotNull private final Field field;

    @Override
    public @NotNull AbstractConfig getConfig() {
        return config;
    }

    @Override
    public void setValue(@NotNull Object value) {
        try {
            this.getField().set(this.getConfig(), value);
        } catch (final Exception exception) {
            throw new ConfigSetException("Failed to set value " + value + " for config " + getConfig() + " (on field " + getField().getName() + ")", exception);
        }
    }

    @Override
    public @NotNull Class<?> getHoldingGenericType() {
        return HoldingGenericParser.getSingleGenericType(field.getType());
    }
}
