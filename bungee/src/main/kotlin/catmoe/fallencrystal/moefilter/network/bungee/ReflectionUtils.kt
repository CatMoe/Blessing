/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
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

package catmoe.fallencrystal.moefilter.network.bungee

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.netty.PipelineUtils
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

// Borrowed from :
// https://github.com/jonesdevelopment/sonar/blob/main/src/main/java/jones/sonar/bungee/util/Reflection.java
class ReflectionUtils {
    @Suppress("DEPRECATION")
    fun inject(initializer: ChannelInitializer<Channel>): AtomicBoolean {
        val version = version
        if (version < 8) {
            ProxyServer.getInstance().stop()
            throw UnsupportedClassVersionError("Reflection failed because java version is unsupported.")
        }
        val success = AtomicBoolean(false)
        Arrays.stream(PipelineUtils::class.java.declaredFields).filter { obj: Field? -> Objects.nonNull(obj) }
            .filter { field: Field -> field.name.equals("server_child", ignoreCase = true) }
            .forEach { field: Field ->
                if (version == 8) {
                    field.isAccessible = true
                    try {
                        val modifier = Field::class.java.getDeclaredField("modifiers")
                        modifier.isAccessible = true
                        modifier.setInt(field, field.modifiers and -0x11)
                        field[PipelineUtils::class.java] = initializer
                        success.set(true)
                    } catch (exception: Exception) { exception.printStackTrace() }
                } else {
                    try {
                        val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
                        unsafeField.isAccessible = true
                        val unsafe = unsafeField[null] as Unsafe
                        unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), initializer)
                        success.set(true)
                    } catch (exception: Exception) { exception.printStackTrace() }
                }
            }
        return success
    }

    val version: Int
        get() = try {
            var version = System.getProperty("java.version")
            if (version.startsWith("1.")) { version = version.substring(2, 3) } else {
                val dot = version.indexOf(".")
                if (dot != -1) { version = version.substring(0, dot) }
            }
            version.toInt()
        } catch (exception: Exception) { 0 }
}
