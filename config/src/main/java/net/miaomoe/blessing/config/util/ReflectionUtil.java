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

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ReflectionUtil {
    @SneakyThrows
    public @NotNull Class<?> getListGenericType(final @NotNull Type genericType) {
        Objects.requireNonNull(genericType);
        final Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
        if (types.length != 1) throw new IllegalArgumentException("There must be only one generic type.");
        return (Class<?>) types[0];
    }

    public @NotNull Class<?> getListGenericType(final @NotNull List<?> list) {
        return getListGenericType(list.getClass().getGenericSuperclass());
    }
}
