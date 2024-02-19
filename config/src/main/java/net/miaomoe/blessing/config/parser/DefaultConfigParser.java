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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.miaomoe.blessing.config.annotation.ConfigValue;
import net.miaomoe.blessing.config.annotation.Description;
import net.miaomoe.blessing.config.annotation.ParseAllField;
import net.miaomoe.blessing.config.getter.ConfigValueGetter;
import net.miaomoe.blessing.config.getter.FieldConfigValueGetter;
import net.miaomoe.blessing.config.getter.MethodConfigValueGetter;
import net.miaomoe.blessing.config.setter.ConfigValueSetter;
import net.miaomoe.blessing.config.setter.FieldConfigValueSetter;
import net.miaomoe.blessing.config.setter.MethodConfigValueSetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultConfigParser implements ConfigParser {

    @Getter
    private static final DefaultConfigParser instance = new DefaultConfigParser();

    @Override
    @SneakyThrows
    public void parse(@NotNull AbstractConfig config) {
        final List<ParsedConfigValue> list = config.getParsedValues();
        list.clear();
        final Class<? extends AbstractConfig> configClass = config.getClass();
        final ParseAllField a = configClass.getAnnotation(ParseAllField.class);
        final boolean parseAll = a != null;
        final List<String> ignore = parseAll ? Arrays.asList(a.ignore()) : null;
        for (Field field : config.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            final @Nullable ConfigValue annotation = field.getAnnotation(ConfigValue.class);
            if (Modifier.isStatic(field.getModifiers()) || (annotation == null && !(parseAll && !ignore.contains(field.getName())))) continue;
            final String path = annotation != null
                    ? annotation.path().isEmpty() ? field.getName() : annotation.path()
                    : field.getName();
            final Description description = field.getAnnotation(Description.class);
            list.add(new ParsedConfigValue(
                    path, field.getType(),
                    description == null ? null : Arrays.asList(description.description()),
                    // it like annotation == null ? true : annotation#useGetter/Setter
                    getValueGetter(config, field, annotation == null || annotation.useGetter()),
                    getValueSetter(config, field, annotation == null || annotation.useSetter())
            ));
        }
    }

    private @NotNull ConfigValueGetter getValueGetter(
            final @NotNull AbstractConfig config,
            final @NotNull Field field,
            final boolean useGetter
    ) {
        if (useGetter) {
            try {
                final String name = (field.getType() == Boolean.class ? "is" : "get") + capitalizeFirstLetter(field.getName());
                return new MethodConfigValueGetter(config, config.getClass().getMethod(name));
            } catch (final NoSuchMethodException ignore) {
                return getValueGetter(config, field, false);
            }
        } else return new FieldConfigValueGetter(config, field);
    }

    private @NotNull ConfigValueSetter getValueSetter(
            final @NotNull AbstractConfig config,
            final @NotNull Field field,
            final boolean useSetter
    ) {
        if (useSetter) {
            try {
                final String name = "set" + capitalizeFirstLetter(field.getName());
                return new MethodConfigValueSetter(config, config.getClass().getMethod(name, field.getType()));
            } catch (final NoSuchMethodException ignore) {
                return getValueSetter(config, field, false);
            }
        } else return new FieldConfigValueSetter(config, field);
    }

    private @NotNull String capitalizeFirstLetter(final @NotNull String input) {
        return input.isEmpty() ? input : Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}
