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
