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

package catmoe.fallencrystal.translation.command.bungee

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.command.ICommandAdapter
import catmoe.fallencrystal.translation.command.TranslationCommand
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.executor.CommandExecutor
import catmoe.fallencrystal.translation.executor.bungee.BungeeConsole
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import java.util.concurrent.CompletableFuture

@Platform(ProxyPlatform.BUNGEE)
class BungeeCommandAdapter(val command: TranslationCommand) : ICommandAdapter {

    constructor(command: TranslationCommand, plugin: Plugin) : this(command) {
        this.plugin=plugin
    }

    var plugin = TranslationLoader.instance.loader.getPluginInstance() as Plugin
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
                is TranslatePlayer -> (sender.upstream as BungeePlayer).player
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