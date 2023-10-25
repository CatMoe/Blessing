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

package catmoe.fallencrystal.moefilter.network.bungee.util.event

enum class EventCallMode {
    AFTER_INIT, // This will call event when connection incoming. Whether they are blocked by throttle or by firewall or not.
    NON_FIREWALL, // When the connection is not blocked by a firewall. It will call event. (priority: firewall > throttle)
    READY_DECODING, // Call event before decoder. If is canceled. We will close the pipeline. (Throttle may close the connection first.)
    AFTER_DECODER, // Call the event after the base pipeline process and decoder. ( not recommend )
    DISABLED // Call the void :D. to save performance.
}