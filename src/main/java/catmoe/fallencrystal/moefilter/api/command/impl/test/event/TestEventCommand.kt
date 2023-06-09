package catmoe.fallencrystal.moefilter.api.command.impl.test.event

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.command.annotation.*
import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.TestMessageEvent
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender

@Command("testevent")
@ConsoleCanExecute
@CommandDescription(DescriptionFrom.STRING, "测试事件是否正常工作.")
@CommandUsage(["/moefilter testevent", "/moefilter testevent <message>"])
@CommandPermission("moefilter.testevent")
@DebugCommand // annotation that is debug command. when registering, if debug is false. this command will be ignored.
class TestEventCommand : ICommand {

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (args!!.size < 2) {
            // args == 1
            EventManager.triggerEvent(TestMessageEvent(sender, ChatMessageType.ACTION_BAR, "test"))
        } else {
            EventManager.triggerEvent(TestMessageEvent(sender, ChatMessageType.ACTION_BAR,
                MessageUtil.messageBuilder(1, args).toString()))
        }
    }

    override fun tabComplete(sender: CommandSender): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = listOf("<message>")
        return map
    }
}