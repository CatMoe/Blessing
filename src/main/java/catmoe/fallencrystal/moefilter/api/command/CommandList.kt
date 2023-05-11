package catmoe.fallencrystal.moefilter.api.command

import com.github.benmanes.caffeine.cache.Caffeine

object CommandList {
    private val commandList = Caffeine.newBuilder().build<String, ICommand>()
    private val iCommand: MutableList<ICommand>
    private val commands: MutableList<String>

    init { iCommand = ArrayList(); commands = ArrayList() }

    fun register(c: ICommand) {
        val command = c.command()
        if (commandList.getIfPresent(command) != null && iCommand.contains(c) && commands.contains(command)) throw ConcurrentModificationException("$c is already registered!")
        iCommand.add(c)
        commandList.put(command, c)
        commands.add(command)
    }

    fun unregister(c: ICommand) {
        val command = c.command()
        if (commandList.getIfPresent(command) == null && !iCommand.contains(c) && !commands.contains(command)) throw NullPointerException("$c is not registered!")
        iCommand.remove(c)
        commandList.invalidate(c.command())
        commands.remove(command)
    }

    fun isRegistered(c: ICommand): Boolean { return commandList.getIfPresent(c.command()) != null && iCommand.contains(c) && commands.contains(c.command()) }

    fun iCommandList(): MutableList<ICommand> { return iCommand }

    fun commandList(): List<String> {
        // 虽然我知道这很可能是Unreachable Code 但为了保证代码质量
        return if (commands.isEmpty()) listOf("?") else commands
    }

    fun getICommand(command: String): ICommand? { return commandList.getIfPresent(command) }
}