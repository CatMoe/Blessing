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

package catmoe.fallencrystal.moefilter.network.common.kick

enum class DisconnectType(@JvmField val messagePath: String) {
    ALREADY_ONLINE("already-online"),
    REJOIN("rejoin"),
    PING("ping"),
    INVALID_NAME("invalid-name"),
    INVALID_HOST("invalid-host"),
    COUNTRY("country"),
    PROXY("proxy"),
    UNEXPECTED_PING("unexpected-ping"),
    DETECTED("detected"),
    PASSED_CHECK("passed-check"),
    RECHECK("recheck"),
    BRAND_NOT_ALLOWED("brand-not-allowed"),
    CANNOT_CHAT("cannot-chat"),
    UNSUPPORTED_VERSION("unsupported-version"),
}
