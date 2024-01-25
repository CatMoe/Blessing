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

package net.miaomoe.blessing.fallback.handler.motd

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.kyori.adventure.text.Component
import net.miaomoe.blessing.fallback.util.ComponentUtil.toJsonElement
import net.miaomoe.blessing.fallback.util.ComponentUtil.toLegacyText
import net.miaomoe.blessing.protocol.version.Version
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Suppress("SpellCheckingInspection")
data class MotdInfo @JvmOverloads constructor(
    val version: VersionInfo,
    val players: PlayerInfo?,
    val description: JsonElement,
    val favicon: Favicon? = null,
    val modinfo: ModInfo? = null
) {

    constructor(
        version: VersionInfo,
        players: PlayerInfo?,
        description: Component
    ) : this(
        version, players, description, null
    )

    constructor(
        version: VersionInfo,
        players: PlayerInfo?,
        description: Component,
        favicon: Favicon?
    ) : this(version, players, description, favicon, null)

    constructor(
        version: VersionInfo,
        players: PlayerInfo?,
        description: Component,
        favicon: Favicon?,
        modinfo: ModInfo?
    ) : this (
        version,
        players,
        description.toJsonElement(),
        favicon,
        modinfo
    )

    data class VersionInfo(
        val name: String,
        val protocol: Int
    ) {
        constructor(
            name: String,
            version: Version
        ) : this(name, version.protocolId)

        constructor(
            name: Component,
            version: Int
        ) : this(name.toLegacyText(), version)

        constructor(
            name: Component,
            version: Version
        ) : this(name, version.protocolId)
    }

    data class PlayerInfo @JvmOverloads constructor(
        val max: Int,
        val online: Int,
        val sample: List<Sample> = listOf()
    )

    data class Sample @JvmOverloads constructor(
        val uuid: UUID = UUID(0, 0),
        val name: String
    ) {
        constructor(uuid: UUID, name: Component) : this(uuid, name.toLegacyText())
        constructor(name: Component) : this(UUID(0, 0), name)
    }

    data class Favicon(val encoded: String?) {

        constructor(image: BufferedImage) : this(toEncodeString(image))

        class FaviconTypeAdapter internal constructor() : TypeAdapter<Favicon>() {

            override fun write(out: JsonWriter, value: Favicon?) {
                val encoded = value?.encoded
                if (encoded == null) out.nullValue() else out.value(encoded)
            }

            override fun read(`in`: JsonReader): Favicon? {
                val peek = `in`.peek()
                return if (peek == JsonToken.NULL) {
                    `in`.nextNull()
                    null
                } else {
                    val encoded: String? = `in`.nextString()
                    if (encoded == null) null else Favicon(encoded)
                }
            }
        }

        companion object {

            private val adapter = FaviconTypeAdapter()

            @JvmStatic
            private fun toEncodeString(image: BufferedImage): String {
                require(image.width == 64 && image.height == 64)
                { "Server icon must be exactly 64x64 pixels!" }
                val imageBytes = ByteArrayOutputStream().use {
                    ImageIO.write(image, "PNG", it)
                    it.toByteArray()
                }
                val encoded = "data:image/png;base64,${Base64.getEncoder().encodeToString(imageBytes)}"
                require(encoded.length > Short.MAX_VALUE)
                { "Encoded favicon too large!" }
                return encoded
            }

            @JvmStatic
            fun getFaviconTypeAdapter() = adapter
        }
    }

    // FML
    data class ModInfo @JvmOverloads constructor(
        val type: String, val modList: List<ModItem> = listOf()
    ) {

        constructor(type: Type) : this(type, listOf())

        constructor(type: Type, modList: List<ModItem>) : this(type.name, modList)

        enum class Type {
            FML,
            VANILLA,
            BUKKIT,
            UNKNOWN
        }

    }

    data class ModItem(val modid: String, val version: String)

    fun toJson(): String = gson.toJson(this)

    companion object {
        val gson = Gson()
    }

}