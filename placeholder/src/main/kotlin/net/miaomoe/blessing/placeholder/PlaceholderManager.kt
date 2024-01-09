/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.placeholder

import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.CommandSender
import net.miaomoe.blessing.event.EventManager
import net.miaomoe.blessing.placeholder.event.PlaceholderRequestEvent
import net.miaomoe.blessing.placeholder.impl.PlayerPlaceholder
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("MemberVisibilityCanBePrivate")
object PlaceholderManager {

    // Match "%?_?...%" and exclude "\%"
    private val regex = Regex("""(?<!\\)%[^%\\]+?(?<!\\)%""")
    private val cache = Caffeine.newBuilder().build<String /* Identifier */, PlaceholderExpansion>()
    private val list = mutableListOf<PlaceholderExpansion>()

    private var logger: Logger? = null
    fun setLogger(logger: Logger) { this.logger=logger }

    init {
        PlayerPlaceholder.let(::register)
    }

    @JvmOverloads
    fun <T : PlaceholderExpansion> register(expansion: T, callback: Consumer<T>? = null) {
        val identifier = expansion.identifier().lowercase()
        require(cache.getIfPresent(identifier) == null) { "This identifier is already registered!" }
        cache.put(identifier, expansion)
        list.add(expansion)
        callback?.accept(expansion)
    }

    @JvmOverloads
    fun <T : PlaceholderExpansion> unregister(expansion: T, callback: Consumer<T>? = null) {
        val identifier = expansion.identifier()
        cache.invalidate(identifier.lowercase())
        var target: PlaceholderExpansion? = null
        for (registered in list) if (registered.identifier() == identifier) { target=registered; break }
        if (target != null) list.remove(target)
        callback?.accept(expansion)
    }

    // Set single placeholder (That is, the input is the placeholder that needs to be requested.)
    //
    // If to replace multiple placeholders or aren't sure if the input is necessarily a placeholder.
    // Use the getPlaceholders() method.
    @JvmOverloads
    fun getSinglePlaceholder(target: CommandSender?, input: String, returnSelfIfNull: Boolean = true): String? {
        val i = input.removeSurrounding("%")
        var result = if (returnSelfIfNull) input else null
        run {  // Call the event. If canceled, return result (null)
            var cancelled = false
            EventManager.call(PlaceholderRequestEvent(target = target, input = i)) {
                // This function is triggered when the event is called. (it = PlaceholderRequestEvent)
                if (it.isCancelled) cancelled=true
            }
            if (cancelled) return result
        }
        if (i.contains("_")) {
            try {
                val identifier = i.split("_")[0]
                cache.getIfPresent(identifier)
                    ?.request(target, i.removePrefix("${identifier}_"))
                    ?.let { result=it }
            } catch (exception: Exception) {
                logger.let {
                    if (it != null)
                        it.log(Level.INFO, "A exception occurred when getting placeholder. (Target: ${target?.name}, Input: $i)", exception)
                    else
                        exception.printStackTrace()
                }
            }
        }
        return result
    }

    // Replace the values and keys provided in the map.
    // Then replace the captured placeholders with setSinglePlaceholder one by one.
    // And replace "\%" with "%" in the output.
    @JvmOverloads
    fun getPlaceholders(target: CommandSender?, input: String, replaces: Map<String, String>? = null): String {
        var output = input
        replaces?.takeUnless { replaces.isEmpty() }?.let { for ((key, value) in it) output=output.replace(key, value) }
        for (match in regex.findAll(output)) {
            val value = match.value
            this.getSinglePlaceholder(target, value, true)?.let { output=output.replace(value, it) }
        }
         return output.replace("\\%", "%")
    }

}