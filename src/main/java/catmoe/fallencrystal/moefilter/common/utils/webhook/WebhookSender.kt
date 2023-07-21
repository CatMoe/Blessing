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

import catmoe.fallencrystal.moefilter.common.utils.webhook.embed.EmbedObject
import java.awt.Color
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

    private fun run(url: String, username: String) {
        try {
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
