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

package catmoe.fallencrystal.moefilter.network.bungee.pipeline

@Suppress("unused")
interface IPipeline {
    companion object {
        const val HANDLER = "moefilter-handler"
        const val DECODER = "moefilter-decoder"
        const val PACKET_INTERCEPTOR = "moefilter-packet-interceptor"
        const val LAST_PACKET_INTERCEPTOR = "moefilter-packet-exception-interceptor"
    }
}
