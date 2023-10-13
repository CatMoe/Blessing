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

package catmoe.fallencrystal.moefilter.network.limbo.packet.common

import catmoe.fallencrystal.moefilter.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.check.info.impl.Brand
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.version.Version
import com.google.common.base.Preconditions
import io.netty.buffer.Unpooled
import io.netty.channel.Channel

// 注: 我仍在研究1.7的PluginMessage, 因此对此暂时禁用
class PacketPluginMessage : LimboPacket {


    var channel = ""
    var message = ""

    var data: ByteArray? = null


    override fun encode(packet: ByteMessage, version: Version?) {
        listOf(this.channel, this.message).forEach { packet.writeString(it) }
    }

    /*
    虽然解码插件通道信息本来是用于识别brand的

    一些愚蠢的机器人也不会发送PluginMessage
    我也不会明白那些一昧监听客户端设置来检测是否是机器人的玩意
    这都2023年了 我不认为还有机器人不发客户端设置的数据包 相反 大部分(机器)人不会发PluginMessage

    也或许有别的方法可以兼容一些模组 甚至是被修改过的Forge ——向它们的客户端的插件通道写入模组信息
     */
    override fun decode(packet: ByteMessage, channel: Channel, version: Version?) {
        this.channel = packet.readString()
        if (version == Version.V1_7_6) packet.readShort() // Ignored
        Preconditions.checkArgument(packet.readableBytes() < 32767, "Payload is too large")
        val data = ByteArray(packet.readableBytes())
        if (this.channel == "MC|Brand" || this.channel == "minecraft:brand") {
            packet.readBytes(data)
            /*
            val m = byteArrayOf((data.size - 1).toByte())
            System.arraycopy(data, 1, m, 0, m.size)

            @Akkariin:
            题外话: 为什么使用ByteArray而不是byteArrayOf :
            ByteArray和byteArrayOf实际上有两种不同的作用
            ByteArray(?) : 创建一个带有长度为?的空字节组
            byteArrayOf(vararg ?) : 创建一个或一段带有?的字节组 (最终它们的对象其实也是ByteArray)
            希望我以后不要再犯这种错误.. ;w; 在区块数据包上我就因为这些搞砸了许多次
             */
            val b = ByteMessage(Unpooled.wrappedBuffer(data))
            message = b.readString()
            b.release()
            // 错误抛出必须在release()后面 避免内存泄漏
            //if (message.isEmpty() || message.length > 128 || message.contains("jndi")) throw IllegalArgumentException("Invalid brand data.")
            Preconditions.checkArgument(!(message.isEmpty() || message.length > 128 || message.contains("jndi")), "Invalid brand data.")
            //MoeLimbo.debug(handler,"Brand: $message")
        }
    }

    override fun handle(handler: LimboHandler) {
        if (this.channel == "MC|Brand" || this.channel == "minecraft:brand") {
            handler.brand=this.message
            if (BrandCheck.increase(Brand(this.message))) {
                FastDisconnect.disconnect(handler, DisconnectType.BRAND_NOT_ALLOWED)
            }
            MoeLimbo.debug(handler, "Brand: $message")
        }
    }

    override fun toString(): String {
        return "PacketPluginMessage(channel=$channel, message=$message)"
    }

}