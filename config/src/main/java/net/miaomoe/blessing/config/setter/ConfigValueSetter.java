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

import lombok.experimental.UtilityClass;
import net.miaomoe.blessing.config.parser.HoldingGenericParser;
import net.miaomoe.blessing.config.parser.ParsedConfigValue;
import net.miaomoe.blessing.config.util.ClassTypeHolder;
import net.miaomoe.blessing.config.util.ConfigHolder;
import net.miaomoe.blessing.config.util.TypeHolder;
import org.jetbrains.annotations.NotNull;

public interface ConfigValueSetter extends HoldingGenericParser, ConfigHolder, TypeHolder {
    void setValue(final @NotNull Object value);

    @UtilityClass
    class NumberSetterUtil {
        public static void setNumber(
                final @NotNull ParsedConfigValue info,
                final @NotNull Number value
        ) {
            final @NotNull ConfigValueSetter setter = info.getSetter();
            final ClassTypeHolder typeHolder = setter.getHoldingType();
            final double result = value.doubleValue();
            if (typeHolder.isDouble()) {
                setter.setValue(result);
            } else if (typeHolder.isInt()) {
                checkArgument(isValid(result, Integer.MAX_VALUE, Integer.MIN_VALUE), "Integer", result);
                setter.setValue((int) result);
            } else if (typeHolder.isFloat()) {
                checkArgument(isValid(result, Float.MAX_VALUE, Float.MIN_VALUE), "Float", result);
                setter.setValue((float) result);
            } else if (typeHolder.isShort()) {
                checkArgument(isValid(result, Short.MAX_VALUE, Short.MIN_VALUE), "Short", result);
                setter.setValue((short) result);
            } else if (typeHolder.isLong()) {
                setter.setValue((long) result);
            } else {
                throw new IllegalArgumentException("Unknown number type: " + value.getClass().getSimpleName());
            }
        }

        private static boolean isValid(final double number, final double maxValue, final double minValue) {
            return number <= maxValue && number >= minValue;
        }

        private static void checkArgument(final boolean result, final @NotNull String type, final @NotNull Object value) {
            if (!result) throw new IllegalArgumentException("Accept " + type + " but the value is outside the supported range. (" + value + ")");
        }
    }
}
