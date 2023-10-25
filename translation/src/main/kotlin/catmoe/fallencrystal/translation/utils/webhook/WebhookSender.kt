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

import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.webhook.embed.EmbedObject
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
