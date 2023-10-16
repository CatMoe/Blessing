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
package catmoe.fallencrystal.translation.command.annotation

import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MoeCommand(
    val name: String,
    val aliases: Array<String> = [],
    val permission: String,
    val allowConsole: Boolean = false,
    val debug: Boolean = false,
    val usage: Array<String> = [],
    /* Description */
    val descType: DescriptionType,
    val descValue: String,
    val asyncExecute: Boolean = true,
)
