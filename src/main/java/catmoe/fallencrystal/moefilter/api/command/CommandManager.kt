package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom.MESSAGE_PATH
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom.STRING
import catmoe.fallencrystal.moefilter.api.command.annotation.parse.ParsedInfo
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.CopyOnWriteArrayList

object CommandManager {
    private val command = Caffeine.newBuilder().build<String, ICommand>()
    private val commands: MutableList<String> = CopyOnWriteArrayList()

    private val parseCommand = Caffeine.newBuilder().build<ICommand, ParsedInfo>()

    private val console = ProxyServer.getInstance().console

    fun register(c: ICommand) {
        val iClass = c::class.java
        if (iClass.isAnnotationPresent(DebugCommand::class.java) && !(try { LocalConfig.getConfig().getBoolean("debug") } catch (_: Exception) { false })) return
        val annotationCommand = ( try { iClass.getAnnotation(Command::class.java).command } catch (_:Exception) { iClass.simpleName.lowercase().replace("command", "") } )
        val annotationPermission = ( try { val permission= iClass.getAnnotation(CommandPermission::class.java).permission; permission.ifEmpty { "moefilter.$annotationCommand" } } catch (_: Exception) { "moefilter.$annotationCommand" } )
        val annotationAllowConsole = iClass.isAnnotationPresent(ConsoleCanExecute::class.java)
        val annotationDescription = getAnnotationDescription(c)
        val annotationUsage = ( try { iClass.getAnnotation(CommandUsage::class.java).usage.toList() } catch (_: Exception) { listOf() } )
        if (commands.contains(annotationCommand)) { return }
        val parsed = ParsedInfo(annotationCommand, annotationDescription, annotationPermission, annotationUsage, annotationAllowConsole)
        parseCommand.put(c, parsed)
        command.put(annotationCommand, c)
        commands.add(annotationCommand)
    }

    private fun getAnnotationDescription(c: ICommand): String {
        val annotationDescription = c::class.java.getAnnotation(CommandDescription::class.java) ?: return ""
        val description = annotationDescription.description
        return when (annotationDescription.type) {
            STRING -> { description }
            MESSAGE_PATH -> { try { LocalConfig.getMessage().getString(description) } catch (_: Exception) { "" } }
        }
    }

    fun unregister(c: ICommand) {
        val iClass = c::class.java
        if (!iClass.isAnnotationPresent(Command::class.java)) return
        val targetCommand = c::class.java.getAnnotation(Command::class.java).command
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