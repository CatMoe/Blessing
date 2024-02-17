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

import net.miaomoe.blessing.config.parser.AbstractConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused"})
public class ClassTypeHolder {
    @Nullable private final Class<?> holdingClassType;

    public ClassTypeHolder() {
        this(null);
    }

    public ClassTypeHolder(@Nullable final Class<?> holdingClassType) {
        this.holdingClassType=holdingClassType;
    }

    public @NotNull Class<?> getHoldingClassType() {
        return Objects.requireNonNull(holdingClassType);
    }

    public boolean isString() {
        return this.getHoldingClassType() == String.class;
    }

    public boolean isInt() {
        return this.getHoldingClassType() == Integer.class;
    }

    public boolean isLong() {
        return this.getHoldingClassType() == Long.class;
    }

    public boolean isFloat() {
        return this.getHoldingClassType() == Float.class;
    }

    public boolean isDouble() {
        return this.getHoldingClassType() == Double.class;
    }

    public boolean isShort() {
        return this.getHoldingClassType() == Short.class;
    }

    public boolean isConfig() {
        return AbstractConfig.class.isAssignableFrom(this.getHoldingClassType());
    }

    public boolean isList() {
        return List.class.isAssignableFrom(this.getHoldingClassType());
    }

    public boolean isEnum() { return Enum.class.isAssignableFrom(this.getHoldingClassType()); }
}
