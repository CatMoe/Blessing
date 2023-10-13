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
import catmoe.fallencrystal.translation.command.TranslationCommand
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.executor.CommandExecutor
import catmoe.fallencrystal.translation.executor.bungee.BungeeConsole
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.concurrent.CompletableFuture

@Platform(ProxyPlatform.BUNGEE)
class BungeeCommandAdapter(val command: TranslationCommand) : CommandAdapter {

    val plugin = TranslationLoader.instance.loader.getPluginInstance() as Plugin
    val proxy = TranslationLoader.instance.loader.getProxyServer().obj as ProxyServer
    var handler: Handler? = null

    override fun register() {
        val clazz = command::class.java
        val info = (if (clazz.isAnnotationPresent(MoeCommand::class.java)) clazz.getAnnotation(MoeCommand::class.java) else null) ?: return
        /*
        val command = if (clazz.isAnnotationPresent(Command::class.java))
            clazz.getAnnotation(Command::class.java).command
        else clazz.simpleName.replace("command", "")
        val aliases = if (clazz.isAnnotationPresent(CommandAliases::class.java))
            clazz.getAnnotation(CommandAliases::class.java).aliases.toMutableList() else mutableListOf(command)
        val permission = if (clazz.isAnnotationPresent(CommandPermission::class.java))
            clazz.getAnnotation(CommandPermission::class.java).permission else ""
        if (!aliases.contains(command)) aliases.add(command)
         */
        val aliases = info.aliases.toMutableList()
        if (!aliases.contains(info.name)) aliases.add(info.name)
        handler=Handler(info.name, info.permission, *(aliases).toTypedArray(), adapter = this)
        proxy.pluginManager.registerCommand(plugin, handler)
    }

    override fun unregister() {
        proxy.pluginManager.unregisterCommand(handler ?: return)
    }

    companion object {
        fun getCastedSender(sender: CommandExecutor): CommandSender? {
            return when (sender) {
                is BungeeConsole -> sender.console
                is TranslatePlayer -> sender.upstream as ProxiedPlayer
                else -> null
            }
        }
    }

}

@Platform(ProxyPlatform.BUNGEE)
class Handler(name: String, permission: String, vararg aliases: String, val adapter: BungeeCommandAdapter) : Command(name, permission, *aliases), TabExecutor {

    val target = adapter.command

    override fun execute(sender: CommandSender, input: Array<out String>) {
        if (sender !is ProxiedPlayer) {
            executeAsync { target.execute(BungeeConsole(sender), input) }
        } else {
            executeAsync { target.execute(PlayerInstance.getPlayer(sender.uniqueId) ?: return@executeAsync, input) }
        }
    }

    private fun executeAsync(runnable: Runnable) {
        if (target::class.java.isAnnotationPresent(MoeCommand::class.java) && target::class.java.getAnnotation(MoeCommand::class.java).asyncExecute)
        CompletableFuture.runAsync { runnable.run() } else runnable.run() }

    override fun onTabComplete(sender: CommandSender, input: Array<out String>): List<String> {
        return if (sender !is ProxiedPlayer) {
            target.tabComplete(BungeeConsole(sender), input)
        } else {
            target.tabComplete(PlayerInstance.getPlayer(sender.uniqueId) ?: return listOf(), input)
        }
    }

}