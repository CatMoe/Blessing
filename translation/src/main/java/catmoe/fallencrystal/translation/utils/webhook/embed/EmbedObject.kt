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
package catmoe.fallencrystal.translation.utils.webhook.embed

import catmoe.fallencrystal.translation.utils.webhook.embed.impl.*
import java.awt.Color

@Suppress("unused")
class EmbedObject {
    val fields: List<Field> = ArrayList()
    var title: String? = null
    var description: String? = null
    var url: String? = null
    var color: Color? = null
    var footer: Footer? = null
    var thumbnail: Thumbnail? = null
    var image: Image? = null
    var author: Author? = null
    fun setTitle(title: String): EmbedObject {
        this.title = title
        return this
    }

    fun setDescription(description: String): EmbedObject {
        this.description = description
        return this
    }

    fun setUrl(url: String): EmbedObject {
        this.url = url
        return this
    }

    fun setColor(color: Color?): EmbedObject {
        this.color = color
        return this
    }

    fun setFooter(text: String, icon: String): EmbedObject {
        footer = Footer(text, icon)
        return this
    }

    fun setThumbnail(url: String): EmbedObject {
        thumbnail = Thumbnail(url)
        return this
    }

    fun setImage(url: String): EmbedObject {
        image = Image(url)
        return this
    }

    fun setAuthor(name: String, url: String, icon: String): EmbedObject {
        author = Author(name, url, icon)
        return this
    }
}