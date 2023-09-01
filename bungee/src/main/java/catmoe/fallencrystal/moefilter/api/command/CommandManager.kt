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

package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.command.annotation.MoeCommand
import catmoe.fallencrystal.translation.command.annotation.misc.DescriptionType.*
import catmoe.fallencrystal.translation.command.annotation.misc.ParsedInfo
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import com.typesafe.config.ConfigException
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused")
object CommandManager {
    private val command = Caffeine.newBuilder().build<String, ICommand>()
    private val commands: MutableList<String> = CopyOnWriteArrayList()

    private val parseCommand = Caffeine.newBuilder().build<ICommand, ParsedInfo>()

    private val console = ProxyServer.getInstance().console

    fun register(c: ICommand) {
        if (!c::class.java.isAnnotationPresent(MoeCommand::class.java)) return
        val info = c::class.java.getAnnotation(MoeCommand::class.java)
        if (commands.contains(info.name)) return
        if (info.debug && !LocalConfig.getConfig().getBoolean("debug")) return
        var description = info.descValue
        if (info.descType != STRING && info.descValue.isNotEmpty()) {
            val config = when (info.descType) {
                CONFIG -> LocalConfig.getConfig()
                MESSAGE_CONFIG -> LocalConfig.getMessage()
                ANTIBOT_CONFIG -> LocalConfig.getAntibot()
                LIMBO_CONFIG -> LocalConfig.getLimbo()
                PROXY_CONFIG -> LocalConfig.getProxy()
                else -> throw IllegalArgumentException("Cannot get config for ${info.descType.name} type!")
            }
            try { description=config.getString(description) } catch (exception: ConfigException) {
                MessageUtil.logWarn("[MoeFilter] Failed to set command desc: ${exception.message}, Using original key $description to description.")
            }
        }
        parseCommand.put(c, ParsedInfo(info.name, description, info.permission, info.usage.toList(), info.allowConsole))
        command.put(info.name, c)
        commands.add(info.name)
    }

    fun unregister(c: ICommand) {
        val iClass = c::class.java
        if (!iClass.isAnnotationPresent(MoeCommand::class.java)) return
        val targetCommand = c::class.java.getAnnotation(MoeCommand::class.java).name
        val originalCommand = command.getIfPresent(targetCommand)
        if (targetCommand.isNotEmpty() && originalCommand != null) {
            parseCommand.invalidate(originalCommand)
            command.invalidate(targetCommand)
        }
        commands.remove(targetCommand)
    }

    fun dropAll() {
        parseCommand.invalidateAll()
        command.invalidateAll()
        commands.clear()
    }

    fun getCommandList(): MutableList<ICommand> {
        val list: MutableList<ICommand> = ArrayList()
        commands.forEach {
            val iCommand = command.getIfPresent(it)
            if (iCommand != null) { list.add(iCommand) }
        }
        return list
    }

    fun getICommand(cmd: String): ICommand? { return command.getIfPresent(cmd) }

    fun getParsedCommand(c: ICommand): ParsedInfo? { return parseCommand.getIfPresent(c) }

    fun getCommandList(sender: CommandSender): MutableList<ICommand> {
        val listWithPermission = mutableListOf<ICommand>()
        getCommandList().forEach {
            val parsedInfo = getParsedCommand(it)!!
            if (sender == console) { if (parsedInfo.allowConsole) listWithPermission.add(it) } else if (sender.hasPermission(parsedInfo.permission)) listWithPermission.add(it)
        }
        return listWithPermission
    }
}
