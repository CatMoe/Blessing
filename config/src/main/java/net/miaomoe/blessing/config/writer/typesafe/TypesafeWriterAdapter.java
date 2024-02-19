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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.DefaultConfigParser;
import net.miaomoe.blessing.config.parser.ParsedConfigValue;
import net.miaomoe.blessing.config.util.ClassTypeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static com.typesafe.config.ConfigValueFactory.fromAnyRef;
import static com.typesafe.config.ConfigValueFactory.fromMap;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class TypesafeWriterAdapter {

    private static final Pattern regex1 = Pattern.compile("\\s*# hardcoded value");
    private static final Pattern regex2 = Pattern.compile("\\h*(# <\\|)");

    final boolean json;

    protected void write(
            final @NotNull Map<String, Object> map,
            final @NotNull AbstractConfig config,
            final boolean fixPrefixSpace
    ) {
        for (final @NotNull ParsedConfigValue value : config.getParsedValues()) {
            final @Nullable Object result = value.getGetter().getValue();
            if (result == null) continue;
            write(map, value, result, fixPrefixSpace);
        }
    }

    protected void write(
            final @NotNull Map<String, Object> map, // key, value
            final @NotNull ParsedConfigValue info,
            final @NotNull Object value,
            final boolean fixPrefixSpace
    ) {
        String description = json ? null : getDesc(info.getDescription(), fixPrefixSpace);
        final String path = info.getPath();
        if (value instanceof List) {
            if (new ClassTypeHolder(info.getGetter().getHoldingGenericType()).isConfig()) {
                final List<Map<String, Object>> list = new ArrayList<>();
                for (final Object element : (List<?>) value) {
                    final AbstractConfig subConfig = (AbstractConfig) element;
                    DefaultConfigParser.getInstance().parse(subConfig);
                    final Map<String, Object> subMap = new LinkedHashMap<>();
                    write(subMap, subConfig, false);
                    list.add(subMap);
                }
                if (!list.isEmpty()) map.put(path, list);
            } else {
                final List<ConfigValue> castedList = new ArrayList<>();
                for (final Object object : (List<?>) value) {
                    if (object == null) continue;
                    castedList.add(fromAnyRef(object, null));
                }
                map.put(path, fromAnyRef(castedList, description));
            }
        } else if (value instanceof Enum)
            map.put(path, fromAnyRef(((Enum<?>) value).name().toUpperCase(Locale.ROOT), description));
        else if (value instanceof AbstractConfig) {
            final AbstractConfig subConfig = (AbstractConfig) value;
            DefaultConfigParser.getInstance().parse(subConfig);
            final Map<String, Object> anotherMap = new LinkedHashMap<>();
            write(anotherMap, subConfig, false);
            map.put(path, fromMap(anotherMap, description));
        } else if (value instanceof Map) {
            throw new UnsupportedOperationException("Unsupported Map object now. Please create AbstractConfig to replace.");
        } else
            map.put(path, fromAnyRef(value, description));
    }

    @Nullable
    private static String getDesc(@Nullable List<String> description, boolean fixPrefixSpace) {
        String desc = null;
        if (description != null && !description.isEmpty()) {
            final int descriptionSize = description.size();
            if (descriptionSize >= 2) {
                final StringBuilder sb = new StringBuilder();
                int i = 0;
                for (final @NotNull String line : description) {
                    sb.append(line);
                    i++;
                    if (i < descriptionSize) {
                        sb.append("\n");
                        if (fixPrefixSpace) sb.append("<|");
                    }
                }
                desc = sb.toString();
            } else {
                desc = description.get(0);
            }
            if (fixPrefixSpace) desc = "<|" + desc;
        }
        return desc;
    }

    protected @NotNull String toString(final @NotNull Config config) {
        String output = config
                .root()
                .render(ConfigRenderOptions
                        .defaults()
                        .setOriginComments(!json)
                        .setFormatted(true)
                        .setJson(json)
                        .setComments(false)
                        .setShowEnvVariableValues(true)
                );
        if (!json) {
            output = output
                    .replaceAll(regex1.pattern(), "")
                    .replaceAll(regex2.pattern(), "# ");
            output = output.startsWith("\n") ? output.substring(1) : output;
        }
        return output;
    }

    protected @NotNull String toString(final @NotNull AbstractConfig config) {
        final Map<String, Object> map = new LinkedHashMap<>();
        write(map, config, true);
        return toString(ConfigFactory.parseMap(map));
    }

}
