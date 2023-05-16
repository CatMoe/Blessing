package catmoe.fallencrystal.moefilter.api.command.impl.test.event

import catmoe.fallencrystal.moefilter.api.command.ICommand
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.TestMessageEvent
import catmoe.fallencrystal.moefilter.util.MessageUtil
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender

class TestEventCommand : ICommand {
    override fun command(): String { return "testevent" }

    override fun allowedConsole(): Boolean { return true }

    override fun description(): String { return "测试事件是否正常工作." }

    override fun permission(): String { return "moefilter.testevent" }

    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (args!!.size < 2) {
            EventManager.triggerEvent(TestMessageEvent(sender, ChatMessageType.ACTION_BAR, ""))
        } else {
            EventManager.triggerEvent(TestMessageEvent(sender, ChatMessageType.ACTION_BAR,
                MessageUtil.messageBuilder(2, args).toString()
            ))
        }
    }

    override fun tabComplete(): MutableMap<Int, List<String>> {
        val map: MutableMap<Int, List<String>> = HashMap()
        map[1] = listOf("<message>")
        return map
    }
}