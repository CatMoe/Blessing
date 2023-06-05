package catmoe.fallencrystal.moefilter.api.command

import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.CommandSender
import java.util.concurrent.CopyOnWriteArrayList

object OCommand {
    private val command = Caffeine.newBuilder().build<String, ICommand>()
    private val commands: MutableList<String> = CopyOnWriteArrayList()

    fun register(c: ICommand) {
        if (commands.contains(c.command())) { return }
        command.put(c.command(), c)
        commands.add(c.command())
    }

    fun unregister(c: ICommand) {
        command.invalidate(c.command())
        commands.remove(c.command())
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

    fun getCommandList(sender: CommandSender): MutableList<ICommand> {
        val listWithPermission = mutableListOf<ICommand>()
        for (it in getCommandList()) { if (sender.hasPermission(it.permission())) { listWithPermission.add(it) } }
        return listWithPermission
    }
}