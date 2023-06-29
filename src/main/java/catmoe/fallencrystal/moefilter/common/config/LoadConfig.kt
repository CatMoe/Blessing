package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.util.CreateConfig
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import com.typesafe.config.Config
import net.md_5.bungee.api.ProxyServer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.concurrent.schedule

class LoadConfig {
    private val path = MoeFilter.instance.dataFolder.absolutePath

    private val configFile = File(path, "config.conf")
    private val messageFile = File(path, "message.conf")
    private val proxyFile = File(path, "proxy.conf")
    private val antibotFile = File(path, "antibot.conf")

    private val version = MoeFilter.instance.description.version

    private val proxy = ProxyServer.getInstance()

    init { instance=this }

    private val defaultConfig = """
                version="$version"
                # 启用调试? 启用后可以获得更多的用法用于调试
                # 不要在生产环境中使用这个! 它可能会泄露你的服务器的关键信息.
                debug=false
                
                # 快速启动. 但不支持重载附属BungeeCord插件以及插件本身(例如BungeePluginManagerPlus重载).
                fastboot=true
                
                # f3服务端标识. 在较高版本的客户端上按F3即可看到.
                # gradient和其它标签e.x <newline> 不可在此处使用
                # 占位符: %bungee%: BungeeCord名称, %version%: 版本名称, %backend%: 后端服务器名称
                f3-brand="<light_purple>MoeFilter <aqua><- <green>%backend%"
                
                # TCP FAST OPEN (TFO) 配置. 该选项仅当antibot.conf中的mode为PIPELINE时有效
                # 仅当服务器为Linux并且启用了Epoll时此选项才有效! 如果您不知道这是什么 建议默认为0
                # 0 = DISABLED, 1 = CLIENT, 2 = SERVER, 3 = BOTH, 4 = MANGLED
                tfo-mode=0
                
                # Ping选项 该选项仅当antibot.conf中的mode为PIPELINE时有效
                ping {
                    # 缓存选项
                    cache {
                        # 缓存有效时间. 单位为秒
                        max-life-time: 5
                        # 即使缓存了也呼叫ProxyPingEvent. 如果返回内容不同 则刷新缓存
                        still-call-event=true
                        # 是否为独立的域创建MOTD缓存?
                        stable-domain-cache=true
                    }
                    # 服务端标识选项 通常会在客户端版本不支持时显示
                    # 仅支持经典MC16色.
                    brand="Requires MC 1.8 - 1.20"
                    # 无论协议是否支持都向客户端发送不支持的协议? 这么做会导致brand总是显示在motd中.
                    protocol-always-unsupported=false
                    # 当Ping到达throttle时 使用缓存而不是为每个请求都呼叫ProxyPingEvent
                    disable-calling-throttle=10
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
                kick {
                    placeholders {
                        custom1="这个是属于你的占位符! 在消息中使用[custom1]来使用它."
                        custom2="你可以增加无限行占位符 来使你的消息模板化更加轻松!"
                    }
                    # 在此处指定占位符格式. %[placeholder]% 就是为 %placeholder% 例如%custom1%就会返回上面的值
                    placeholder-pattern="%[placeholder]%"
                    already-online=[
                        "",
                        "<red>You are already connected this server!",
                        "<red>Contact server administrator for more information.",
                        ""
                    ]
                    rejoin=[
                        "",
                        "<green>You are successfully passed first-join check",
                        "<white>Please rejoin server to join.",
                        ""
                    ]
                    ping=[
                        "",
                        "<yellow>Please ping server first.",
                        ""
                    ]
                    invalid-name=[
                        "",
                        "<red>Your name is invalid or not allowed on this server.",
                        ""
                    ]
                }
            """.trimIndent()

    private val defaultAntiBot = """
                version="$version"
                
                # 反机器人工作模式.
                # PIPELINE(推荐): 接管BungeeCord管道 实现无效数据包检查和EVENT模式拥有的所有检查
                 # 并且比EVENT更快. 但无法保证所有东西都兼容. 有关兼容的BungeeCord 请移步:
                 # https://github.com/CatMoe/MoeFilter#%E5%AE%8C%E5%85%A8%E5%85%BC%E5%AE%B9%E7%9A%84bungeecord
                 # EVENT: 使用传统事件组合的反机器人. 可以实现一些普通的检查 例如PingJoin, FirstJoin, etc
                 # 理论上兼容所有BungeeCord分叉. 如果您在使用PIPELINE时出现了一些问题 可以选择使用该模式.
                 # DISABLED: 什么也不做. (作为纯实用工具使用)
                mode=PIPELINE
                
                # 选择事件呼叫时机 该选项仅当mode为PIPELINE时有效.
                # AFTER_INIT: 当连接传入时立马呼叫事件 无论它们是否被阻止 (不推荐)
                # NON_FIREWALL: 当连接没被防火墙切断时呼叫事件
                # READY_DECODING: 在解码器解码前呼叫事件 (推荐)
                # AFTER_DECODER: 当解码器完成解码后呼叫事件 (不推荐, 但如果您正在使用反向代理(e.x HAProxy) 请使用此模式或DISABLED)
                # DISABLED: 不呼叫事件以保留性能(推荐 但如果遇到问题 请将其设为以上模式的其中一种.)
                event-call-mode=DISABLED
                
                general {
                    # RECONNECT: 仅重新连接
                    # JOIN_AFTER_PING: Ping后加入
                    # RECONNECT_AFTER_PING: Ping后重新连接
                    # PING_AFTER_RECONNECT: 重新连接后Ping
                    # STABLE: 独立模块互相工作
                    # DISABLED: 禁用
                    join-ping-mixin-mode=PING_AFTER_RECONNECT
                    
                    // 有效名称正则. 默认正则的规则
                    // 即名称不能包含mcstorm, mcdown或bot字样. 名称只能含有数字 字母以及下划线 且长度限制在3-16
                    valid-regex="^(?!.*(?:mcstorm|mcdown|bot))[A-Za-z0-9_]{3,16}${'$'}"
                }
    """.trimIndent()

    private val defaultProxy = """
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
        val config = LocalConfig.getConfig()
        val message = LocalConfig.getMessage()
        val proxy = LocalConfig.getProxy()
        val antibot = LocalConfig.getAntibot()
        if (config.getString("version") != version || config.isEmpty) { updateConfig("config", config) }
        if (message.getString("version") != version || message.isEmpty) { updateConfig("message", message) }
        if (proxy.getString("version") != version || proxy.isEmpty) { updateConfig("proxy", proxy) }
        if (antibot.getString("version") != version || antibot.isEmpty) { updateConfig("antibot", antibot) }
        LocalConfig.reloadConfig()
    }

    private fun updateConfig(file: String, config: Config) {
        val version = config.getString("version")
        val nowTime = System.currentTimeMillis()
        val oldFile = Paths.get("$path/$file.conf")
        val newFile = Paths.get("$path/$file-old-$version-$nowTime.conf")
        Files.move(oldFile, newFile)
        createDefaultConfig()
        broadcastUpdate(newFile)
    }

    private fun createDefaultConfig() {
        val pluginFolder = MoeFilter.instance.dataFolder
        val defaultConfigMap = mapOf(
            configFile to defaultConfig,
            messageFile to defaultMessage,
            proxyFile to defaultProxy,
            antibotFile to defaultAntiBot
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

    fun getAntibot(): File { return antibotFile }

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

    companion object {
        lateinit var instance: LoadConfig
            private set
    }
}