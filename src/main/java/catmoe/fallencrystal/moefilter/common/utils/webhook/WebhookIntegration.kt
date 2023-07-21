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
package catmoe.fallencrystal.moefilter.common.utils.webhook

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.utils.webhook.embed.EmbedObject
import java.io.IOException
import java.lang.reflect.Array
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Suppress("MemberVisibilityCanBePrivate")
open class WebhookIntegration {
    @JvmField
    val embeds: MutableList<EmbedObject> = ArrayList()
    @JvmField
    var webhookUrl: String? = null

    var content: String? = null

    var username: String? = null

    protected var avatarUrl: String? = null

    private val proxyConf = LocalConfig.getProxy().getConfig("proxies-config")

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
        val proxyType = try { Proxy.Type.valueOf(proxyConf.getAnyRef("mode").toString()) } catch (_: Exception) { Proxy.Type.DIRECT }
        val proxy = if (proxyType != Proxy.Type.DIRECT) { Proxy(proxyType, InetSocketAddress(proxyConf.getString("host"), proxyConf.getInt("port"))) } else null
        val connection = (if (proxy != null) url.openConnection(proxy) else url.openConnection() ) as HttpsURLConnection
        connection.addRequestProperty("Content-Type", "application/json")
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

    @Suppress("MemberVisibilityCanBePrivate")
    internal inner class JSONObject {
        private val map = HashMap<String, Any>()
        fun put(s: String, value: Any?) {
            if (value != null) {
                map[s] = value
            }
        }

        override fun toString(): String {
            val builder = StringBuilder()
            val entrySet: Set<Map.Entry<String, Any>> = map.entries
            builder.append("{")
            var i = 0
            for ((s, obj) in entrySet) {
                builder.append(quote(s)).append(":")
                if (obj is String) { builder.append(quote(obj.toString()))
                } else if (obj is Int) { builder.append(Integer.valueOf(obj.toString()))
                } else if (obj is Boolean) { builder.append(obj)
                } else if (obj is JSONObject) { builder.append(obj.toString())
                } else if (obj.javaClass.isArray) {
                    builder.append("[")
                    val len = Array.getLength(obj)
                    for (j in 0 until len) { builder.append(Array.get(obj, j).toString()).append(if (j != len - 1) "," else "") }
                    builder.append("]")
                }
                builder.append(if (++i == entrySet.size) "}" else ",")
            }
            return builder.toString()
        }

        fun quote(string: String): String {
            return "\"" + string + "\""
        }
    }
}
