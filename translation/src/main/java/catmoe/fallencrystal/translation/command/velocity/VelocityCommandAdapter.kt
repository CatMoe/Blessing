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

package catmoe.fallencrystal.translation.command.velocity

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.command.CommandAdapter
import catmoe.fallencrystal.translation.command.ICommand
import catmoe.fallencrystal.translation.command.annotation.AsyncExecute
import catmoe.fallencrystal.translation.command.annotation.Command
import catmoe.fallencrystal.translation.command.annotation.CommandAliases
import catmoe.fallencrystal.translation.command.annotation.CommandPermission
import catmoe.fallencrystal.translation.executor.velocity.VelocityConsole
import catmoe.fallencrystal.translation.player.PlayerInstance
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.CompletableFuture

class VelocityCommandAdapter(val command: ICommand) : CommandAdapter {

    val plugin = TranslationLoader.instance.loader.getPluginInstance()
    val proxy = TranslationLoader.instance.loader.getProxyServer().obj as ProxyServer
    private val manager = proxy.commandManager
    var permission = ""
    private var handler: Handler? = null
    private var meta: CommandMeta? = null

    override fun register() {
        val clazz = command::class.java
        val command = if (clazz.isAnnotationPresent(Command::class.java))
            clazz.getAnnotation(Command::class.java).command
        else clazz.simpleName.replace("command", "")
        val aliases = if (clazz.isAnnotationPresent(CommandAliases::class.java))
            clazz.getAnnotation(CommandAliases::class.java).aliases.toMutableList() else mutableListOf(command)
        permission = if (clazz.isAnnotationPresent(CommandPermission::class.java))
            clazz.getAnnotation(CommandPermission::class.java).permission else ""
        if (!aliases.contains(command)) aliases.add(command)
        handler=Handler(this)
        meta=manager.metaBuilder(command)
            .aliases(*(aliases).toTypedArray())
            .plugin(plugin)
            .build()
        manager.register(meta, handler)
    }

    override fun unregister() { manager.unregister(meta ?: return) }
}
class Handler(private val adapter: VelocityCommandAdapter) : SimpleCommand {

    private val target = adapter.command
    private val async = target::class.java.isAnnotationPresent(AsyncExecute::class.java)

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