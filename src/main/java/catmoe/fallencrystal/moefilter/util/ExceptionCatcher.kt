package catmoe.fallencrystal.moefilter.util

import catmoe.fallencrystal.moefilter.api.logger.ILogger
import java.util.logging.LogRecord

class ExceptionCatcher : ILogger {

    // 这是一个ExceptionCatcher(x 是一个如何使用ILogger接口和LoggerManager的举例

    private val exception: List<String> = listOf(
        /*
        我们要获取全部的Exception消息 在这里检查并过滤掉
        (为什么没有消息? 咕咕咕w=)
        然后CPS+1
        虽然我知道这相当于差不多半吊子检查
        因为没写修改监听管道那么高级的东西
        但总比垃圾邮件刷后台强
         */
        ""
    )

    override fun isLoggable(record: LogRecord?): Boolean {
        val message = record!!.message
        exception.forEach { if (message.contains(it)) exceptionTriggered(message); return false }
        return true
    }

    private fun exceptionTriggered(log: String) {
        TODO()
    }

}