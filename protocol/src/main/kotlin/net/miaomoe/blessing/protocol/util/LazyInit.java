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

package net.miaomoe.blessing.protocol.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A lazy util. When a target is needed, caches and returns the specified object.
 * @param <T>
 * @author FallenCrystal
 */
public class LazyInit<T> {

    private final Supplier<T> init;
    private boolean already = false;
    private T value = null;

    public LazyInit(@NotNull final Supplier<T> init) {
        this.init=init;
    }

    /**
     * Whether the init.get() method has been used.
     * @return boolean
     */
    public boolean isAlreadyLoaded() {
        return already;
    }

    /**
     * Get the value.
     * If not loaded, Will use init.get() first.
     * @return T
     */
    public T getValue() {
        final T value = already ? this.value : init.get();
        this.value=value;
        already=true;
        return value;
    }

    /**
     * Get value directly and ignoring loaded.
     * @return T
     */
    public @Nullable T getValueDirectly() {
        return value;
    }

    @Override
    public String toString() {
        return "LazyInit(alreadyLoaded=" + already + ", value=" + value + ")";
    }
}
