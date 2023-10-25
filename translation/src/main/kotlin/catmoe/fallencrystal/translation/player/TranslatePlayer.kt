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

package catmoe.fallencrystal.translation.player

// 由于类的一些问题 我们需要用一个用于兼容的类来包裹那些
// 尽管如此 也可以通过 is 来判断是VelocityPlayer还是BungeePlayer并获取它们原本的属性.
@Suppress("MemberVisibilityCanBePrivate")
class TranslatePlayer(val upstream: PlatformPlayer): PlatformPlayer by upstream