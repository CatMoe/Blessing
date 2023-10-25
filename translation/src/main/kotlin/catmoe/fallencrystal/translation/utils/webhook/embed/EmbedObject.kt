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