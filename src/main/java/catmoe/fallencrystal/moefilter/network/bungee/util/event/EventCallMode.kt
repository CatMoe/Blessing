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

package catmoe.fallencrystal.moefilter.network.bungee.util.event

enum class EventCallMode {
    AFTER_INIT, // This will call event when connection incoming. Whether they are blocked by throttle or by firewall or not.
    NON_FIREWALL, // When the connection is not blocked by a firewall. It will call event. (priority: firewall > throttle)
    READY_DECODING, // Call event before decoder. If is canceled. We will close the pipeline. (Throttle may close the connection first.)
    AFTER_DECODER, // Call the event after the base pipeline process and decoder. ( not recommend )
    DISABLED // Call the void :D. to save performance.
}