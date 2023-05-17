package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.util.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.concurrent.schedule

object LoadConfig {
    private val path = FilterPlugin.getDataFolder()!!.absolutePath

    private val configFile = File(path, "config.conf")
    private val messageFile = File(path, "message.conf")

    private val version = FilterPlugin.getPlugin()!!.description.version

    private val proxy = ProxyServer.getInstance()

    private val defaultConfig = """
                version="$version"
                # 启用调试? 启用后可以获得更多的用法用于调试
                # 不要在生产环境中使用这个! 它可能会泄露你的服务器的关键信息.
                debug=true
                # 配置模块应该怎么样工作
                # 
                #  ALWAYS: 始终启用
                #  DURING-ATTACK: 仅在攻击时启用
                #  DISABLED: 始终禁用
                # 
                # 如果您填入了意外的值 则默认开启.
                checks-type {
                    FIRST-JOIN=ALWAYS
                    PING-JOIN=DURING-ATTACK
                }
            """.trimIndent()

    private val defaultMessage = """
                version="$version"
                prefix="&bMoe&fFilter &7>> "
                reload-warn="&e您可以使用重载命令重载配置文件. 但实际上这可能会意外地破坏某些东西. 如果可以 请尽快重启代理而非使用reload命令."
                command {
                    not-found="&c未找到命令."
                    no-permission="&c缺少权限: [permission]"
                    description {
                        reload="快速重载MoeFilter配置文件(不推荐)"
                        help="列出所有已注册的命令 并针对指定命令提供帮助"
                    }
                }
                blacklist-reason {
                    ADMIN="被管理员列入黑名单"
                    PROXY="疑似使用代理或VPN"
                    PING-LIMIT="短时间内Ping次数过多"
                    JOIN-LIMIT="短时间内尝试加入次数过多"
                    ALTS="同地址拥有太多账户"
                }
            """.trimIndent()

    fun loadConfig() {
        createDefaultConfig()
        val config = ObjectConfig.getConfig()
        val message = ObjectConfig.getMessage()
        val pluginFolder = FilterPlugin.getDataFolder()!!.absolutePath
        val configVersion = config.getString("version")
        val messageVersion = message.getString("version")
        val nowTime = System.currentTimeMillis()
        if (configVersion != version || config.isEmpty) {
            val oldFile = Paths.get("$pluginFolder/config.conf")
            val newFile = Paths.get("$pluginFolder/config-old-$configVersion-$nowTime.conf")
            Files.move(oldFile, newFile)
            createDefaultConfig()
            broadcastUpdate(newFile)
        }
        if (messageVersion != version || message.isEmpty) {
            val oldFile = Paths.get("$pluginFolder/message.conf")
            val newFile = Paths.get("$pluginFolder/message-old-$configVersion-$nowTime.conf")
            Files.move(oldFile, newFile)
            createDefaultConfig()
            broadcastUpdate(newFile)
        }
        ObjectConfig.reloadConfig()
    }

    private fun createDefaultConfig() {
        var createConfig = false
        var createMessage = false
        val pluginFolder = FilterPlugin.getDataFolder()!!
        if (!pluginFolder.exists()) { pluginFolder.mkdirs() }
        if (!configFile.exists()) { createConfig = true }
        if (!messageFile.exists()) { createMessage = true }
        if (createConfig) { configFile.createNewFile(); configFile.writeText(defaultConfig) }
        if (createMessage) { messageFile.createNewFile(); messageFile.writeText(defaultMessage) }
    }

    fun getConfigFile(): File { return configFile }

    fun getMessage(): File { return messageFile }

    private fun broadcastUpdate(newFile: Path) {
        val message: List<String> = listOf(
            "-------------------- MoeFilter --------------------",
            "您的配置文件或语言文件版本不同于您当前使用的MoeFilter版本.",
            "或者您的配置文件为空.",
            "",
            "我们已将您旧的配置文件重命名并生成新的一份.",
            "旧的配置文件将被命名成 ${newFile.fileName}",
            "",
            "代理将在10秒后关闭 避免插件可能不按照您的预期工作.",
            "即使您仍然不修改任何内容. 在下次启动时也会使用默认的配置文件.",
            "-------------------- MoeFilter --------------------"
        )
        message.forEach { MessageUtil.logWarn(it) }
        Timer().schedule(10000L) { proxy.stop() }
    }
}