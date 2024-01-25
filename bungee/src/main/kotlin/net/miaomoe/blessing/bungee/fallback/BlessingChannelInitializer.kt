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
package net.miaomoe.blessing.bungee.fallback

import io.netty.channel.*
import net.miaomoe.blessing.bungee.BlessingBungee
import net.miaomoe.blessing.fallback.handler.FallbackInitializer
import net.miaomoe.blessing.protocol.registry.State
import sun.misc.Unsafe
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
object BlessingChannelInitializer : ChannelInitializer<Channel>() {

    private val plugin = BlessingBungee.instance

    private val state = State.entries // init

    val writeMarker = WriteBufferWaterMark(1 shl 20, 1 shl 21)

    val fallbackInitializer = FallbackInitializer(
        BlessingBungee.config.fallback,
        motdHandler = BungeeMotdAdapter,
        initCachedPacket = true
    )

    private lateinit var originalInstance: ChannelInitializer<*>
    private lateinit var originalMethod: Method

    // Test fallback?
    override fun initChannel(channel: Channel) {
        channel.config().let {
            try { it.setOption(ChannelOption.IP_TOS, 0x18) } catch (_: ChannelException) {}
            it.setOption(ChannelOption.TCP_NODELAY, true)
            it.setWriteBufferWaterMark(writeMarker)
        }
        fallbackInitializer.initChannel(channel)
    }

    fun handleOriginal(channel: Channel) {
        originalMethod.invoke(originalInstance, channel)
    }

    fun inject() {
        val pipelineUtils = Class.forName("net.md_5.bungee.netty.PipelineUtils")
        val childField = pipelineUtils.getDeclaredField("SERVER_CHILD")
        val original = childField[null] as ChannelInitializer<*>
        val clazz = original::class.java
        clazz.name.let {
            plugin.logger.log(Level.INFO, it)
            require(it == "net.md_5.bungee.netty.PipelineUtils\$1")
            { "Unsupported modified detected. Please delete another plugin about network (e.x. antibot)" }
        }
        originalInstance = original
        clazz.getDeclaredMethod("initChannel", Channel::class.java).let {
            it.isAccessible=true
            originalMethod = it
        }
        childField.isAccessible=true
        if (Modifier.isFinal(childField.modifiers)) {
            val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").let {
                it.isAccessible=true
                it[null] as Unsafe
            }
            unsafe.putObject(unsafe.staticFieldBase(childField), unsafe.staticFieldOffset(childField), this)
        } else
            childField[null] = this
    }
}