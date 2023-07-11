/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.bungee.util

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

    private val version: Int
        get() = try {
            var version = System.getProperty("java.version")
            if (version.startsWith("1.")) { version = version.substring(2, 3) } else {
                val dot = version.indexOf(".")
                if (dot != -1) { version = version.substring(0, dot) }
            }
            version.toInt()
        } catch (exception: Exception) { 0 }
}
