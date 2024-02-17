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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.miaomoe.blessing.config.parser.AbstractConfig;
import net.miaomoe.blessing.config.parser.DefaultConfigParser;
import net.miaomoe.blessing.config.parser.ParsedConfigValue;
import net.miaomoe.blessing.config.util.ClassTypeHolder;
import net.miaomoe.blessing.config.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static com.typesafe.config.ConfigValueFactory.fromAnyRef;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class TypesafeWriterAdapter {

    private static final Pattern regex1 = Pattern.compile("\\s*# hardcoded value");
    private static final Pattern regex2 = Pattern.compile("\\h*(# <\\|)");

    final boolean json;

    protected void write(
            final @NotNull Map<String, Object> map,
            final @NotNull AbstractConfig config,
            final @Nullable String parent,
            final boolean fixPrefixSpace
    ) {
        for (final @NotNull ParsedConfigValue value : config.getParsedValues()) {
            final @NotNull String path = (parent == null ? "" : parent) + value.getPath();
            final @Nullable Object result = value.getGetter().getValue();
            if (result == null) continue;
            write(map, path, result, value.getDescription(), fixPrefixSpace);
        }
    }

    protected void write(
            final @NotNull Map<String, Object> map, // key, value
            final @NotNull String path,
            final @NotNull Object value,
            final @Nullable List<String> description,
            final boolean fixPrefixSpace
    ) {
        String desc = json ? null : getDesc(description, fixPrefixSpace);
        if (value instanceof List) {
            if (new ClassTypeHolder(ReflectionUtil.getListGenericType((List<?>) value)).isConfig()) {
                final List<Map<String, Object>> list = new ArrayList<>();
                for (final Object element : (List<?>) value) {
                    final AbstractConfig subConfig = (AbstractConfig) element;
                    DefaultConfigParser.getInstance().parse(subConfig);
                    final Map<String, Object> subMap = new LinkedHashMap<>();
                    write(subMap, subConfig, null, false);
                    list.add(subMap);
                }
                if (!list.isEmpty()) map.put(path, list);
            } else map.put(path, desc == null ? value : fromAnyRef(value, desc));
        } else if (value instanceof Enum)
            map.put(path, ((Enum<?>) value).name().toUpperCase(Locale.ROOT));
        else if (value instanceof AbstractConfig) {
            final AbstractConfig subConfig = (AbstractConfig) value;
            DefaultConfigParser.getInstance().parse(subConfig);
            if (desc == null)
                write(map, subConfig, path + ".", false);
            else  {
                final Map<String, Object> anotherMap = new LinkedHashMap<>();
                write(anotherMap, subConfig, path, false);
                map.put(path, fromAnyRef(anotherMap, desc));
            }
        } else if (value instanceof Map) {
            throw new UnsupportedOperationException("Unsupported Map object now. Please create AbstractConfig to replace.");
        } else
            map.put(path, desc == null ? value : fromAnyRef(value, desc));
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
        write(map, config, null, true);
        return toString(ConfigFactory.parseMap(map));
    }

}
