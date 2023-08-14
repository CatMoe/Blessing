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
import com.typesafe.config.Config
import java.awt.Color
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WebhookSender {
    private val webhook = WebhookIntegration()
    fun sendWebhook(
        content: String,
        title: String,
        color: Color,
        embed: Boolean,
        url: String,
        ping: String,
        username: String
    ) {
        if (ping.isNotEmpty()) {
            webhook.content = "<@$ping>"
            run(url, username)
        }
        if (embed) {
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
            val localDateTime = LocalDateTime.now()
            webhook.embeds.add(
                EmbedObject()
                    .setDescription(content)
                    .setTitle(title)
                    .setColor(color)
                    .setFooter(formatter.format(localDateTime), "")
            )
        } else {
            webhook.content = content
        }
        run(url, username)
    }

    fun sendWebhook(config: Config) {
        val enabled = config.getBoolean("enabled")
        if (!enabled) return
        val url = config.getString("url")
        val username = config.getString("username")
        val ping = config.getString("ping")
        val title = config.getString("title")
        val format = config.getStringList("format").joinToString("\n")
        val embed = config.getBoolean("embed.enabled")
        val color = Color(config.getInt("embed.color.r"), config.getInt("embed.color.g"), config.getInt("embed.color.b"))
        sendWebhook(format, title, color, embed, url, ping, username)
    }

    private fun proxyFromCfg(): Proxy? {
        val proxyConf = LocalConfig.getProxy().getConfig("proxies-config")
        val proxyType =
            try { Proxy.Type.valueOf(proxyConf.getAnyRef("mode").toString()) }
            catch (_: Exception) { Proxy.Type.DIRECT }
        return if (proxyType != Proxy.Type.DIRECT) {
            Proxy(proxyType, InetSocketAddress(proxyConf.getString("host"), proxyConf.getInt("port")))
        } else null
    }

    private fun run(url: String, username: String) {
        try {
            webhook.proxy = if (webhook.proxy != null) webhook.proxy else if (proxyFromCfg() != null) proxyFromCfg() else null
            webhook.webhookUrl = url
            webhook.username = username
            webhook.execute()
            webhook.embeds.clear()
            webhook.content = null
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
