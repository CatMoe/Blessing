package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.common.config.util.CreateConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.typesafe.config.Config
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
    private val proxyFile = File(path, "proxy.conf")

    private val version = FilterPlugin.getPlugin()!!.description.version

    private val proxy = ProxyServer.getInstance()

    private val defaultConfig = """
                version="$version"
                # 启用调试? 启用后可以获得更多的用法用于调试
                # 不要在生产环境中使用这个! 它可能会泄露你的服务器的关键信息.
                debug=false
                
                # 快速启动. 但不支持重载附属BungeeCord插件以及插件本身(例如BungeePluginManagerPlus重载).
                fastboot=true
                
                # 实用工具模式, 启用后 禁用所有反机器人功能
                # 不要使用多个反机器人. 我们提供让你选择这个的权利.
                util-mode=false
                
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
                # API v6已经切换到MiniMessage.
                prefix="<gradient:#F9A8FF:#97FFFF>MoeFilter</gradient> <gray>>> "
                reload-warn="<yellow>您可以使用重载命令重载配置文件. 但实际上这可能会意外地破坏某些东西. 如果可以 请尽快重启代理而非使用reload命令."
                
                actionbar {
                    # 可用占位符: 
                    # %process_cpu% (BungeeCord CPU用量) %system_cpu% (系统使用CPU用量)
                    # %cps% (每秒涌入连接数) %ipsec% (每秒涌入连接的ip数) %peak_cps% (历史最高每秒涌入连接数) %total% (共涌入连接数)
                    # %prefix% (返回上面的prefix)
                    # 更多占位符会随着功能的增加而添加.
                    style="%prefix%<gradient:green:yellow:aqua> CPU proc. %process_cpu%% sys. %system_cpu%% - CPS %cps% - Peak %peak_cps% - IpSec %ipsec% - Total %total%</gradient>"
                    # 更新频率 (tick为单位)
                    update-delay=1
                    command {
                        description="在actionbar查看MoeFilter状态."
                        enable="<hover:show_text:'<red>点击来切换actionbar'><click:run_command:/moefilter actionbar><green>已切换actionbar</click>"
                        disable="<hover:show_text:'<green>点击来切换actionbar'><click:run_command:/moefilter actionbar><red>已切换actionbar</click>"
                    }
                }
                
                # [permission] = 当前命令权限占位符
                command {
                    not-found="<red>未找到命令."
                    no-permission="<red>缺少权限: [permission]"
                    only-player="<red>该命令仅可以由在线玩家执行."
                    # 各类子命令描述
                    description {
                        reload="快速重载MoeFilter配置文件(不推荐)"
                        help="列出所有已注册的命令 并针对指定命令提供帮助"
                    }
                    # 命令补全描述. 颜色符号无法在此处使用.
                    tabComplete {
                        # 当玩家没有权限使用MoeFilter的命令时 应该返回什么tab补全
                        no-permission="You don't have permission to use this command."
                        # 当玩家没有权限使用MoeFilter的命令但补全了它时 应该返回什么tab补全. 
                        no-subcommand-permission="You need permission [permission] to use this command."
                    }
                    # 当玩家没有权限使用那个子命令时 是否完全隐藏命令 (启用后忽略tabComplete的"no-subcommand-permission"和"no-permission")
                    full-hide-command=false
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
                        "BLACKLISTED  [reason]",
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

    private val proxiesConfig = """
                version="$version"
        
                # 内置的反代理. 自动从网站上抓取代理并使用换成记录
                internal: {
                    enabled=true
                    # 启用调试 (大量垃圾邮件警告!)
                    debug=false
                    # 调度器任务设置
                    schedule {
                        # schedule总是会在启动时触发. 所以你不需要担心启动时不同步.
                        # 单位为小时 意味着每三小时更新一次.
                        update-delay=3
                    }
                    lists = [
                        "https://api.proxyscrape.com/?request=getproxies&proxytype=http&timeout=10000&country=all&ssl=all&anonymity=all",
                        "https://www.proxy-list.download/api/v1/get?type=http",
                        "https://www.proxy-list.download/api/v1/get?type=https",
                        "https://www.proxy-list.download/api/v1/get?type=socks4",
                        "https://www.proxy-list.download/api/v1/get?type=socks5",
                        "https://shieldcommunity.net/sockets.txt",
                        "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/http.txt",
                        "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks4.txt",
                        "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks5.txt",
                        "https://raw.githubusercontent.com/ShiftyTR/Proxy-List/master/http.txt",
                        "https://raw.githubusercontent.com/ShiftyTR/Proxy-List/master/https.txt",
                        "https://raw.githubusercontent.com/ShiftyTR/Proxy-List/master/socks4.txt",
                        "https://raw.githubusercontent.com/ShiftyTR/Proxy-List/master/socks5.txt",
                        "https://raw.githubusercontent.com/monosans/proxy-list/main/proxies/http.txt",
                        "https://raw.githubusercontent.com/monosans/proxy-list/main/proxies/socks4.txt",
                        "https://raw.githubusercontent.com/monosans/proxy-list/main/proxies/socks5.txt",
                        "https://raw.githubusercontent.com/jetkai/proxy-list/main/online-proxies/txt/proxies-http.txt",
                        "https://raw.githubusercontent.com/jetkai/proxy-list/main/online-proxies/txt/proxies-https.txt",
                        "https://raw.githubusercontent.com/jetkai/proxy-list/main/online-proxies/txt/proxies-socks4.txt",
                        "https://raw.githubusercontent.com/jetkai/proxy-list/main/online-proxies/txt/proxies-socks5.txt",
                        "https://raw.githubusercontent.com/rdavydov/proxy-list/main/proxies/http.txt",
                        "https://raw.githubusercontent.com/rdavydov/proxy-list/main/proxies/socks4.txt",
                        "https://raw.githubusercontent.com/rdavydov/proxy-list/main/proxies/socks5.txt",
                        "https://raw.githubusercontent.com/clarketm/proxy-list/master/proxy-list-raw.txt",
                        "https://raw.githubusercontent.com/mertguvencli/http-proxy-list/main/proxy-list/data.txt",
                        "https://raw.githubusercontent.com/scriptzteam/ProtonVPN-VPN-IPs/main/exit_ips.txt",
                        "https://raw.githubusercontent.com/scriptzteam/ProtonVPN-VPN-IPs/main/entry_ips.txt",
                        "https://raw.githubusercontent.com/mmpx12/proxy-list/master/http.txt",
                        "https://raw.githubusercontent.com/mmpx12/proxy-list/master/https.txt",
                        "https://raw.githubusercontent.com/mmpx12/proxy-list/master/socks4.txt",
                        "https://raw.githubusercontent.com/mmpx12/proxy-list/master/socks5.txt"
                    ]
                }
                
                # 设置来自https://proxycheck.io的服务
                proxycheck-io {
                    enabled=true
                    # API秘钥 您需要在上面注册一个账户才可以使用该服务.
                    key = [
                        "your-key-here"
                    ]
                    # 黑名单属性. 有VPN(代理), Business(主机供应商/企业), Wireless(家用网络)
                    # 有需要可以将Business也加入 但这会阻止绝大多数加速器 并且有一定的误判风险.
                    # Wireless没有专门用于区分有线网络和移动数据. 如果需要 请移步至IP-API
                    blacklisted-type = ["VPN"]
                    # 跟VPN属性不同. 对于proxycheck那边的解释是 VPN和Proxy不是一种东西 VPN属于虚拟专用网络 而Proxy属于代理.
                    blacklist-proxy=true
                    # 单日可查询次数限制. 这取决于你的计划. 但由于我们并不真正保存使用次数 
                    # 而是作为插件在检查时的请求次数-1 以避免无限制调用接口.
                    # 您可以使用多个API账户秘钥. 当到达throttle时 使用另一个API秘钥检查.
                    limit=1000
                    # 每分钟请求限制.
                    throttle=350
                }
                
                ip-api {
                    enabled=true
                    # 每分钟请求限制 超过此限制后 处理将被跳过或等待.
                    throttle=45
                    blacklist-proxy=true
                }
                
                # GeoIP设置 @MaxMind Database
                country {
                    # MaxMind Database下载许可key. 建议使用自己的许可证秘钥.
                    # https://dev.maxmind.com/geoip/geolite2-free-geolocation-data#accessing-geolite2-free-geolocation-data
                    key="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"
                    # 模式. WHITELIST(白名单), BLACKLIST(黑名单) 或 DISABLED(禁用)
                    mode=DISABLED
                    # 列表
                    list = ["CN"]
                }
                
                # 代理上网配置. 适用于所有API和内置反代理 爬取
                # 暂不支持非HTTP/HTTPS以及SOCKS5之外的代理.
                proxies-config {
                    # 工作模式. HTTP: HTTP代理, SOCKS: socks5代理, DIRECT: 直连
                    mode=DIRECT
                    host="localhost"
                    port=8080
                }
                
    """.trimIndent()

    fun loadConfig() {
        createDefaultConfig()
        val config = ObjectConfig.getConfig()
        val message = ObjectConfig.getMessage()
        val proxy = ObjectConfig.getProxy()
        if (config.getString("version") != version || config.isEmpty) { updateConfig("config", config) }
        if (message.getString("version") != version || message.isEmpty) { updateConfig("message", message) }
        if (proxy.getString("version") != version || proxy.isEmpty) { updateConfig("proxy", proxy) }
        ObjectConfig.reloadConfig()
    }

    private fun updateConfig(file: String, config: Config) {
        val version = config.getString("version")
        val nowTime = System.currentTimeMillis()
        val pluginFolder = FilterPlugin.getDataFolder()!!.absolutePath
        val oldFile = Paths.get("$pluginFolder/$file.conf")
        val newFile = Paths.get("$pluginFolder/$file-old-$version-$nowTime.conf")
        Files.move(oldFile, newFile)
        createDefaultConfig()
        broadcastUpdate(newFile)
    }

    private fun createDefaultConfig() {
        val pluginFolder=FilterPlugin.getDataFolder()!!
        val defaultConfigMap = mapOf(
            configFile to defaultConfig,
            messageFile to defaultMessage,
            proxyFile to proxiesConfig,
        )
        defaultConfigMap.forEach { (file, config) ->
            val createConfig = CreateConfig(pluginFolder)
            createConfig.setDefaultConfig(config)
            createConfig.setConfigFile(file)
            createConfig.onLoad()
        }
    }

    fun getConfigFile(): File { return configFile }

    fun getMessage(): File { return messageFile }

    fun getProxy(): File { return proxyFile }

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