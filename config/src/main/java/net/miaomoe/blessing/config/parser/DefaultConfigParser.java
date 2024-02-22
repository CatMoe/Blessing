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
import net.miaomoe.blessing.config.annotation.Comment;
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

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

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
            final @NotNull String path = annotation != null
                    ? (annotation.path().isEmpty() ? (annotation.autoFormat() ? this.formatPath(field.getName()) : field.getName()) : annotation.path())
                    : this.formatPath(field.getName());
            if (!isValidPath(path)) throw new IllegalArgumentException("Invalid path: \"" + (path.isEmpty() ? "[empty]" : path) + "\"! The path must contain only English characters or numbers.");
            final Comment comment = field.getAnnotation(Comment.class);
            list.add(new ParsedConfigValue(
                    path, field.getType(),
                    comment == null ? null : Arrays.asList(comment.description()),
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

    private @NotNull String formatPath(final @NotNull String input) {
        if (input.length() <= 1) { return input; }
        final StringBuilder result = new StringBuilder();
        final char firstChar = input.charAt(0);
        final char secondChar = input.charAt(1);
        result.append((isUpperCase(firstChar) && !isUpperCase(secondChar)) ? toLowerCase(firstChar) : firstChar);
        for (int i = 1; i < input.length(); i++) {
            final char currentChar = input.charAt(i);
            final char prevChar = input.charAt(i - 1);
            final char nextChar = (i < input.length() - 1) ? input.charAt(i + 1) : '\0';
            if (isUpperCase(currentChar) && !isUpperCase(prevChar) && !isUpperCase(nextChar)) {
                result.append("-");
                result.append(toLowerCase(currentChar));
            } else result.append(currentChar);
        }
        return result.toString();
    }

    private boolean isValidPath(final @NotNull String string) {
        return !string.isEmpty() && string.matches("[a-zA-Z0-9-]+");
    }
}
