package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
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
                
                #
                # Join & Ping 检查.
                # 此模块可以阻止绝大愚蠢的机器人 但这取决于您的配置.
                #
                ping-and-join {
                    # 以下是可用的模式:
                    #
                    # DISABLED  关闭模块
                    # STABLE  两个模块作为独立工作的模块 即客户端可以随时Ping和重新连接 而不会因为顺序不对或用户名之类而判定未通过检查
                    # ONLY_JOIN  玩家只需要重新连接即可加入服务器
                    # ONLY_PING  玩家只需要刷新服务器列表 就可以加入服务器了
                    # PING_FIRST  玩家需要先刷新服务器列表 然后再加入两次服务器 才可以进入服务器
                    # JOIN_FIRST  玩家需要先进入服务器 然后再刷新服务器列表并进入 才可以进入服务器
                    # PING_FIRST_REST  跟PING_FIRST不同的是 如果玩家以同地址不同用户名加入服务器 则判定未通过
                    # JOIN_FIRST_REST  跟PING_FIRST差不多 都会检查玩家的用户名在通过检查时是否保持他们进行检查前的地址和玩家名
                    # 
                    # AUTO (仅在攻击时有效 & 启用后以下两项模式选择均无效)
                    # 当没有任何攻击时 使用JOIN_FIRST_REST
                    # 当攻击模式为"仅加入一次"或"Ping和加入"时 则使用JOIN_FIRST_REST
                    # 当攻击模式为"重新加入时" 则使用PING_FIRST_REST
                    
                    # 当没有激活反机器人模式时 应该使用什么模式
                    IDLE=STABLE_REST
                    # 当服务器遭到攻击时 应该使用什么模式
                    DURING-ATTACK=PING_FIRST_REST
                    
                    # 使用auto模式?
                    AUTO=true
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
                methods {
                    JOIN="Join"
                    ONCE-JOIN="Once Join"
                    REJOIN="Reconnect"
                    PING-AND-JOIN="Ping+Join"
                    LONGER-NAME="Longer name"
                    BAD-NAME="Bad name"
                    
                    PING="Ping"
                    EXCEPTION-PING="Exception Ping"
                    PING-FLOOD="Motd Attack"
                    
                    UNKNOWN-PROTOCOL="Protocol"
                    MALFORMED_PACKET="Packets"
                }
                blacklist-reason {
                    ADMIN="被管理员列入黑名单"
                    PROXY="疑似使用代理或VPN"
                    PING-LIMIT="短时间内Ping次数过多"
                    JOIN-LIMIT="短时间内尝试加入次数过多"
                    CHECK-FAILED="无法验证您是否是机器人还是玩家"
                    ALTS="同地址拥有太多账户"
                }
                kick {
                    placeholders {
                        custom1="这个是属于你的占位符! 在消息中使用[custom1]来使用它."
                        custom2="你可以增加无限行占位符 来使你的消息模板化更加轻松!"
                    }
                    # 在此处指定占位符格式. %[placeholder]% 就是为 %placeholder% 例如%custom1%就会返回上面的值
                    placeholder-pattern="%[placeholder]%"
                    blacklisted = [
                        "",
                        "BLACKLISTED",
                        ""
                    ]
                    rejoin = [
                        "",
                        "REJOIN",
                        ""
                    ]
                    ping = [
                        "",
                        "Ping server first.",
                        ""
                    ]
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