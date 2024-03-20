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

package net.miaomoe.blessing.config.parser;

import lombok.*;
import net.miaomoe.blessing.config.getter.ConfigValueGetter;
import net.miaomoe.blessing.config.setter.ConfigValueSetter;
import net.miaomoe.blessing.config.util.ClassTypeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ParsedConfigValue extends ClassTypeHolder {
    @NotNull String path;
    @NotNull Class<?> type;
    @Nullable List<String> comment;
    @NotNull ConfigValueGetter getter;
    @NotNull ConfigValueSetter setter;
    boolean required;

    @Override
    public @NotNull Class<?> getHoldingClassType() {
        return ClassTypeHolder.primitiveToWrapper(this.type);
    }
}
