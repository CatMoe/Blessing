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

package catmoe.fallencrystal.translation.command.bungee

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.command.CommandAdapter
import catmoe.fallencrystal.translation.command.ICommand
import catmoe.fallencrystal.translation.command.annotation.AsyncExecute
import catmoe.fallencrystal.translation.command.annotation.Command
import catmoe.fallencrystal.translation.command.annotation.CommandAliases
import catmoe.fallencrystal.translation.command.annotation.CommandPermission
import catmoe.fallencrystal.translation.executor.bungee.BungeeConsole
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlayerInstance
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.concurrent.CompletableFuture

@Platform(ProxyPlatform.BUNGEE)
class BungeeCommandAdapter(val command: ICommand) : CommandAdapter {

    val plugin = TranslationLoader.instance.loader.getPluginInstance() as Plugin
    val proxy = TranslationLoader.instance.loader.getProxyServer().obj as ProxyServer
    var handler: Handler? = null

    override fun register() {
        val clazz = command::class.java
        val command = if (clazz.isAnnotationPresent(Command::class.java))
            clazz.getAnnotation(Command::class.java).command
        else clazz.simpleName.replace("command", "")
        val aliases = if (clazz.isAnnotationPresent(CommandAliases::class.java))
            clazz.getAnnotation(CommandAliases::class.java).aliases.toMutableList() else mutableListOf(command)
        val permission = if (clazz.isAnnotationPresent(CommandPermission::class.java))
            clazz.getAnnotation(CommandPermission::class.java).permission else ""
        if (!aliases.contains(command)) aliases.add(command)
        handler=Handler(command, permission, *(aliases).toTypedArray(), adapter = this)
        proxy.pluginManager.registerCommand(plugin, handler)
    }

    override fun unregister() {
        proxy.pluginManager.unregisterCommand(handler ?: return)
    }

}

@Platform(ProxyPlatform.BUNGEE)
class Handler(name: String, permission: String, vararg aliases: String, val adapter: BungeeCommandAdapter) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {

    val target = adapter.command

    override fun execute(sender: CommandSender, input: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            executeAsync { target.execute(BungeeConsole(sender), input) }
        } else {
            executeAsync { target.execute(PlayerInstance.getPlayer(sender.uniqueId) ?: return@executeAsync, input) }
        }
    }

    private fun executeAsync(runnable: Runnable) { if (target::class.java.isAnnotationPresent(AsyncExecute::class.java)) CompletableFuture.runAsync { runnable.run() } else runnable.run() }

    override fun onTabComplete(sender: CommandSender, input: Array<out String>): List<String> {
        return if (sender !is ProxiedPlayer) {
            target.tabComplete(BungeeConsole(sender), input)
        } else {
            target.tabComplete(PlayerInstance.getPlayer(sender.uniqueId) ?: return listOf(), input)
        }
    }

}