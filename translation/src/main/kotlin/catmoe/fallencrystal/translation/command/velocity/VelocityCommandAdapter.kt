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

package catmoe.fallencrystal.translation.command.velocity

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.command.ICommandAdapter
import catmoe.fallencrystal.translation.command.TranslationCommand
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.executor.CommandExecutor
import catmoe.fallencrystal.translation.executor.velocity.VelocityConsole
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.velocity.VelocityPlayer
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.CompletableFuture

class VelocityCommandAdapter(val command: TranslationCommand) : ICommandAdapter {

    constructor(command: TranslationCommand, plugin: Any): this(command) {
        this.plugin=plugin
    }

    var plugin: Any = TranslationLoader.instance.loader.getPluginInstance()
    val proxy = TranslationLoader.instance.loader.getProxyServer().obj as ProxyServer
    private val manager = proxy.commandManager
    var permission = ""
    private var handler: Handler? = null
    private var meta: CommandMeta? = null

    override fun register() {
        val clazz = command::class.java
        val info = (if (clazz.isAnnotationPresent(MoeCommand::class.java)) clazz.getAnnotation(MoeCommand::class.java) else null) ?: return
        /*
        val aliases = if (clazz.isAnnotationPresent(CommandAliases::class.java))
            clazz.getAnnotation(CommandAliases::class.java).aliases.toMutableList() else mutableListOf(command)
         */
        val aliases = info.aliases.toMutableList()
        this.permission=info.permission
        if (!aliases.contains(info.name)) aliases.add(info.name)
        handler=Handler(this)
        meta=manager.metaBuilder(info.name)
            .aliases(*(aliases).toTypedArray())
            .plugin(plugin)
            .build()
        manager.register(meta, handler)
    }

    override fun unregister() { manager.unregister(meta ?: return) }

    companion object {
        fun getCastedSender(sender: CommandExecutor): CommandSource? {
            return when (sender) {
                is VelocityConsole -> sender.orig
                is TranslatePlayer -> (sender.upstream as VelocityPlayer).player
                else -> null
            }
        }
    }
}
class Handler(private val adapter: VelocityCommandAdapter) : SimpleCommand {

    private val target = adapter.command
    private val async = target::class.java.getAnnotation(MoeCommand::class.java).asyncExecute

    override fun execute(inv: Invocation) {
        if (inv.source() == adapter.proxy.consoleCommandSource) {
            target.execute(VelocityConsole(inv.source()), inv.arguments())
        } else {
            val tp = PlayerInstance.getPlayer((inv.source() as Player).uniqueId) ?: return
            if (async) {
                CompletableFuture.runAsync { target.execute(tp, inv.arguments()) }
            } else target.execute(tp, inv.arguments())
        }
    }

    override fun hasPermission(invocation: Invocation): Boolean {
        return invocation.source().hasPermission(adapter.permission)
    }

    override fun suggest(invocation: Invocation): MutableList<String> {
        if (async) return mutableListOf()
        return internalSuggest(invocation)
    }

    private fun internalSuggest(invocation: Invocation): MutableList<String> {
        return  if (invocation.source() == adapter.proxy.consoleCommandSource) {
            target.tabComplete(VelocityConsole(invocation.source()), invocation.arguments())
        } else {
            target.tabComplete(PlayerInstance.getPlayer((invocation.source() as Player).uniqueId) ?: return mutableListOf(), invocation.arguments())
        }
    }

    override fun suggestAsync(invocation: Invocation): CompletableFuture<MutableList<String>> {
        if (!async) return super.suggestAsync(invocation)
        return CompletableFuture.completedFuture(suggest(invocation))
    }

}