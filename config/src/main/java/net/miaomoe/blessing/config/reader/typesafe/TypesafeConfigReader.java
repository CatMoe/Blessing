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

package net.miaomoe.blessing.config.reader.typesafe;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.SneakyThrows;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.DefaultConfigParser;
import net.miaomoe.blessing.config.parser.ParsedConfigValue;
import net.miaomoe.blessing.config.reader.ConfigReader;
import net.miaomoe.blessing.config.setter.ConfigSetException;
import net.miaomoe.blessing.config.setter.ConfigValueSetter;
import net.miaomoe.blessing.config.util.ClassTypeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

abstract class TypesafeConfigReader implements ConfigReader {

    @Override
    public void read(@NotNull String context, @NotNull AbstractConfig config) {
        this.read(Objects.requireNonNull(ConfigFactory.parseString(context)), config);
    }

    public void read(@NotNull final Config context, @NotNull AbstractConfig config) {
        for (final @NotNull ParsedConfigValue value : config.getParsedValues()) {
            Objects.requireNonNull(value);
            if (!context.hasPath(value.getPath())) continue;
            Object setValue = getSetValue(value, context);
            if (setValue != null) value.getSetter().setValue(setValue);
        }
    }

    @SneakyThrows
    private @Nullable Object getSetValue(@NotNull final ParsedConfigValue value, @NotNull final Config config) {
        if (value.isConfig()) {
            final AbstractConfig subConfig = (AbstractConfig) Objects.requireNonNull(value.getGetter().getValue());
            DefaultConfigParser.getInstance().parse(subConfig);
            this.read(config.getConfig(value.getPath()), subConfig);
            return null; // Don't set AbstractConfig for them.
        } else if (value.isEnum()) {
            final Method method = value.getHoldingClassType().getMethod("valueOf", String.class);
            return method.invoke(null, config.getString(value.getPath()).toUpperCase(Locale.ROOT));
        } else if (value.isList()) {
            final ClassTypeHolder genericType = new ClassTypeHolder(value.getSetter().getHoldingGenericType());
            if (genericType.isConfig()) {
                final Constructor<?> constructor;
                try {
                    constructor=genericType.getHoldingClassType().getDeclaredConstructor();
                    constructor.setAccessible(true);
                } catch (final Exception exception) {
                    throw new ConfigSetException("Target must be have a empty constructor!", exception);
                }
                final List<AbstractConfig> list = new ArrayList<>();
                for (final @NotNull Config subConfig : config.getConfigList(value.getPath())) {
                    final AbstractConfig newConfig = (AbstractConfig) constructor.newInstance();
                    DefaultConfigParser.getInstance().parse(newConfig);
                    this.read(subConfig, newConfig);
                    list.add(newConfig);
                }
                return list;
            } else return config.getAnyRefList(value.getPath());
        } else if (value.isNumber()) {
            setNumber(value, config.getNumber(value.getPath()));
            return null;
        } else return config.getAnyRef(value.getPath());
    }

    private static void setNumber(
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
