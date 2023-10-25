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
package catmoe.fallencrystal.translation.utils.webhook

import catmoe.fallencrystal.translation.utils.webhook.embed.EmbedObject
import java.io.IOException
import java.lang.reflect.Array
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Suppress("MemberVisibilityCanBePrivate")
open class WebhookIntegration {

    val embeds: MutableList<EmbedObject> = ArrayList()

    var webhookUrl: String? = null

    var content: String? = null

    var username: String? = null

    protected var avatarUrl: String? = null

    var proxy: Proxy? = null

    @Throws(IOException::class)
    fun execute() {
        if (content == null && embeds.isEmpty()) return
        val json = JSONObject()
        json.put("content", content)
        json.put("username", username)
        json.put("avatar_url", avatarUrl)
        json.put("tts", false)
        if (embeds.isNotEmpty()) {
            val embedObjects: MutableList<JSONObject> = ArrayList()
            for (embed in embeds) {
                val jsonEmbed = JSONObject()
                jsonEmbed.put("title", embed.title)
                jsonEmbed.put("description", embed.description)
                jsonEmbed.put("url", embed.url)
                if (embed.color != null) {
                    val color = embed.color
                    var rgb = color!!.red
                    rgb = (rgb shl 8) + color.green
                    rgb = (rgb shl 8) + color.blue
                    jsonEmbed.put("color", rgb)
                }
                val footer = embed.footer
                val image = embed.image
                val thumbnail = embed.thumbnail
                val author = embed.author
                val fields = embed.fields
                if (footer != null) {
                    val jsonFooter = JSONObject()
                    jsonFooter.put("text", footer.text)
                    jsonFooter.put("icon_url", footer.iconUrl)
                    jsonEmbed.put("footer", jsonFooter)
                }
                if (image != null) {
                    val jsonImage = JSONObject()
                    jsonImage.put("url", image.url)
                    jsonEmbed.put("image", jsonImage)
                }
                if (thumbnail != null) {
                    val jsonThumbnail = JSONObject()
                    jsonThumbnail.put("url", thumbnail.url)
                    jsonEmbed.put("thumbnail", jsonThumbnail)
                }
                if (author != null) {
                    val jsonAuthor = JSONObject()
                    jsonAuthor.put("name", author.name)
                    jsonAuthor.put("url", author.url)
                    jsonAuthor.put("icon_url", author.iconUrl)
                    jsonEmbed.put("author", jsonAuthor)
                }
                val jsonFields: MutableList<JSONObject> = ArrayList()
                for (field in fields) {
                    val jsonField = JSONObject()
                    jsonField.put("name", field.name)
                    jsonField.put("value", field.value)
                    jsonField.put("inline", field.inline)
                    jsonFields.add(jsonField)
                }
                jsonEmbed.put("fields", jsonFields.toTypedArray())
                embedObjects.add(jsonEmbed)
            }
            json.put("embeds", embedObjects.toTypedArray())
        }
        val url = URL(webhookUrl)
        val connection = (if (proxy != null) url.openConnection(proxy) else url.openConnection()) as HttpsURLConnection
        connection.addRequestProperty(
            "Content-Type",
            "application/json"
        )
        connection.addRequestProperty(
            "User-Agent",
            "MoeFilter Webhook"
        )
        connection.doOutput = true
        connection.requestMethod = "POST"
        val stream = connection.outputStream
        stream.write(json.toString().toByteArray())
        stream.flush()
        stream.close()
        connection.inputStream.close()
        connection.disconnect()
    }

    internal inner class JSONObject {
        private val map = HashMap<String, Any>()
        fun put(s: String, value: Any?) { if (value != null) { map[s] = value } }

        override fun toString(): String {
            val builder = StringBuilder()
            val entrySet: Set<Map.Entry<String, Any>> = map.entries
            builder.append("{")
            var i = 0
            entrySet.forEach { (it, obj) ->
                builder.append("\"$it\"").append(":")
                /*
                if (obj is String) { builder.append("\"$obj\"") } else if (obj is Int) { builder.append(Integer.valueOf(obj.toString()))
                } else if (obj is Boolean) { builder.append(obj) } else if (obj is JSONObject) { builder.append(obj.toString())
                } else if (obj.javaClass.isArray) {
                    builder.append("[")
                    val len = Array.getLength(obj)
                    for (j in 0 until len) { builder.append(Array.get(obj, j).toString()).append(if (j != len - 1) "," else "") }
                    builder.append("]")
                }
                 */
                when (obj) {
                    is String -> builder.append("\"$obj\"")
                    is Int -> builder.append(Integer.valueOf(obj.toString()))
                    is Boolean -> builder.append(obj)
                    is JSONObject -> builder.append(obj.toString())
                    else -> {
                        if (obj.javaClass.isArray) {
                            builder.append("[")
                            //val len = Array.getLength(obj)
                            val len = Array.getLength(obj)
                            for (j in 0 until len) { builder.append(Array.get(obj, j).toString()).append(if (j != len - 1) "," else "") }
                            builder.append("]")
                        }
                    }
                }
                builder.append(if (++i == entrySet.size) "}" else ",")
            }
            return builder.toString()
        }
    }
}
