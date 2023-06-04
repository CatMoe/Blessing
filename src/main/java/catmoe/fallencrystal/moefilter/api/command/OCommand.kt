package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.CopyOnWriteArrayList

object OCommand {
    private val commandList = Caffeine.newBuilder().build<String, ICommand>()
    private val iCommand: MutableList<ICommand> = CopyOnWriteArrayList()
    private val commands: MutableList<String> = CopyOnWriteArrayList()

    private val scheduler = ProxyServer.getInstance().scheduler
    private val plugin = FilterPlugin.getPlugin()

    fun register(c: ICommand) {
        scheduler.runAsync(plugin) {
            iCommand.forEach { if (c::class.java == it::class.java) { return@runAsync } }
            iCommand.add(c)
        }
    }

    fun unregister(c: ICommand) {
        scheduler.runAsync(plugin) {
            val removeObjectQueue: MutableList<ICommand> = ArrayList()
            iCommand.forEach { if (c::class.java == it::class.java) { removeObjectQueue.add(it) } }
            if (removeObjectQueue.isNotEmpty()) { iCommand.removeAll(removeObjectQueue) }
        }
    }

    fun isRegistered(c: ICommand): Boolean { return commandList.getIfPresent(c.command()) != null && iCommand.contains(c) && commands.contains(c.command()) }

    fun iCommandList(): MutableList<ICommand> { return iCommand }

    fun commandList(): List<String> {
        // 虽然我知道这很可能是Unreachable Code 但为了保证代码质量
        return if (commands.isEmpty()) listOf("?") else commands
    }

    fun getICommand(command: String): ICommand? { return commandList.getIfPresent(command) }

    fun getCommandList(sender: CommandSender): MutableList<ICommand> {
        val list = iCommandList()
        val listWithPermission = mutableListOf<ICommand>()
        for (it in list) { if (sender.hasPermission(it.permission())) { listWithPermission.add(it) } }
        return listWithPermission
    }
}