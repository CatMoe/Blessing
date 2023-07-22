/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.common.config.util.CreateConfig
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.typesafe.config.Config
import net.md_5.bungee.api.ProxyServer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.concurrent.schedule

@Suppress("SpellCheckingInspection")
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
                # 不要在生产环境中使用这个! 它可能会导致发送大量垃圾邮件降低性能或泄露你的服务器的关键信息.
                debug=false
                
                # 快速启动. 但不支持重载附属BungeeCord插件以及插件本身(例如BungeePluginManagerPlus重载).
                fastboot=false
                
                # f3服务端标识. 在较高版本的客户端上按F3即可看到.
                # gradient和其它标签e.x <newline> 不可在此处使用
                # 占位符: %bungee%: BungeeCord名称, %version%: 版本名称, %backend%: 后端服务器名称
                f3-brand="<light_purple>MoeFilter <aqua><- <green>%backend%"
                
                # TCP FAST OPEN (TFO) 配置. 该选项仅当antibot.conf中的mode为PIPELINE时有效
                # 仅当服务器为Linux并且启用了Epoll时此选项才有效! 如果您不知道这是什么 建议默认为0
                # 0 = DISABLED, 1 = CLIENT, 2 = SERVER, 3 = BOTH, 4 = MANGLED
                tfo-mode=0

                domain-check {
                    enabled=false
                    allow-lists=["127.0.0.1", "localhost", "mc.miaomoe.net", "catmoe.realms.moe"]
                }
                
                # Ping选项 该选项仅当antibot.conf中的mode为PIPELINE时有效 (临时废弃)
                ping {
                    # 缓存选项
                    cache {
                        # 缓存有效时间. 单位为秒
                        max-life-time=5
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
                
                # 每秒连接数限制. 当您正在使用PIPELINE模式时非常有用.
                # EVENT模式此throttle关闭连接的效率比BungeeCord自带的低
                throttle-limit=3
                
                # 提醒
                notifications {
                    # discord webhook.
                    # 此选项将会通过proxy.conf中的代理设置发送webhook 而不是直连
                    # 注意! ——如果有人要求您的配置文件 请将此段删除或将url留空 避免遭到webhook垃圾邮件滥用.
                    webhook {
                        # 当在使用/moefilter webhook (debug命令 首先打开debug模式) 时发送的webhook 
                        test {
                            # false = 不发送
                            enabled=false
                            # discord webhook url
                            # 将鼠标悬浮在频道上 然后点击 "编辑频道",
                            # 整合 -> 创建 Webhook -> 创建 Webhook -> 点击 "Captain Hook" -> 
                            # 设置头像和频道 (可选) -> 复制 Webhook URL 然后粘贴在此处
                            url=""
                            # webhook消息的名称 (不遵循discord中的webhook名称设置)
                            username="MoeFilter Webhook"
                            # @ 设置 - 设置此选项之后将会在webhook发送时@某个人/组
                            # 禁用 = 留空
                            # ping用户 = 直接键入用户id
                            # ping组 (role) = &组id (保留& 就像&1234567890这样)
                            # 如何获取id? 设置 -> 高级设置 -> 开发者模式 = 开
                            # 对于用户: 右键用户 点击 "复制用户ID"
                            # 对于role: 左键拥有需要@的role的用户 右键role 然后点击 "复制身份组ID" 
                            ping=""
                            # Webhook标题
                            title="MoeFilter Test Webhook"
                            # 内容
                            format=[
                                "> This is a webhook for test!"
                            ]
                            # 嵌入消息设置
                            embed {
                                # 是否嵌入消息?
                                enabled=true
                                # 嵌入边框颜色设置
                                color {
                                    r=255
                                    g=255
                                    b=180
                                }
                            }
                        }
                        attack-start {
                            enabled=false
                            url=""
                            username="MoeFilter Webhook"
                            ping=""
                            title="MoeFilter"
                            format=[
                                "> :warning: The server is under attack!"
                            ]
                            embed {
                                enabled=true
                                color {
                                    r=255
                                    g=255
                                    b=0
                                }
                            }
                        }
                        attack-stopped {
                            enabled=false
                            url=""
                            username="MoeFilter Webhook"
                            ping=""
                            title="MoeFilter"
                            format=[
                                "> :warning: This server is under attack!"
                            ]
                            embed {
                                enabled=true
                                color {
                                    r=255
                                    g=255
                                    b=0
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

    private val defaultMessage = """
                version="$version"
                # API v6已经切换到MiniMessage.
                prefix="<gradient:#F9A8FF:#97FFFF>MoeFilter</gradient> <gray>>> "
                reload-warn="<green>已重新加载配置文件 部分内容可能需要重启代理生效."
                
                actionbar {
                    # 可用占位符: 
                    # %process_cpu% (BungeeCord CPU用量) %system_cpu% (系统使用CPU用量)
                    # %cps% (每秒涌入连接数) %ipsec% (每秒涌入连接的ip数) %peak_cps% (历史最高每秒涌入连接数) %total% (共涌入连接数)
                    # %prefix% (返回上面的prefix)
                    # 更多占位符会随着功能的增加而添加.
                    format {
                        idle="%prefix%<gradient:green:yellow:aqua> CPU proc. %process_cpu%% sys. %system_cpu%% - CPS %cps% - IpSec %ipsec% - Total %total%</gradient>"
                        attack=""
                    }
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
                        server-ip="mc.miaomoe.net"
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
                    invalid-host=[
                        "",
                        "<red>Please join server from %server-ip%",
                        ""
                    ]
                    country=[
                        "",
                        "<red>Your country is not allowed on this server.",
                        ""
                    ]
                    proxy=[
                        "",
                        "<red>This server is not allowed proxy/vpn.",
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
                
                # 攻击模式激活设置
                attack-mode { 
                    # 当一秒内的连接超过此数字 将激活攻击模式
                    incoming=5
                    # 模式激活设置
                    mode {
                        # 当阻止总数在1秒内达到此值 将会将状态设置为被此模式攻击
                        count=4
                    }
                    un-attacked {
                        # 当1秒内没有任何连接传入时 立即解除攻击模式
                        instant=true
                        # 如果instant模式为false且 x 秒内没有传入连接 则解除戒备模式
                        wait=5
                    }
                }
                
                general {
                    # RECONNECT: 仅重新连接
                    # JOIN_AFTER_PING: Ping后加入
                    # RECONNECT_AFTER_PING: Ping后重新连接
                    # PING_AFTER_RECONNECT: 重新连接后Ping
                    # STABLE: 独立模块互相工作
                    # DISABLED: 禁用
                    join-ping-mixin-mode=RECONNECT
                    
                    # join+ping的检查的缓存过期时间 (秒)
                    # 如果您或您的玩家遇到连续要求的问题
                    # 请尝试将值调高直到适合您或您的玩家通过检查
                    # 但也不要填写一个较大的值.
                    max-cache-time=10
                }
                
                name-check {
                    # 名称有效性检查
                    valid-check {
                        # 有效名称正则. 默认正则的规则. 如需禁用检查 将其设为 "" 即可.
                        # 即名称不能包含mcstorm, mcdown或bot字样. 名称只能含有数字 字母以及下划线 且长度限制在3-16
                        valid-regex="^(?!.*(?:mcstorm|mcdown|bot|cuute))[A-Za-z0-9_]{3,16}${'$'}"
                        # 自动防火墙配置 (此选项将会将触发此检查的玩家列入临时黑名单)
                        # THROTTLE: 当触发此检查时如果刚好触发连接限制 则黑名单
                        # ALWAYS: 始终将触发此检查的IP列入黑名单
                        # ATTACK: 当在遭到攻击时始终将其列入黑名单 (无用, 因为攻击事件等代码未完全实施)
                        # DISABLED: 仅踢出
                        firewall-mode=ALWAYS
                    }
                    
                    # 名称相似性检查
                    # 该检查的踢出理由仍然将是 invalid-name
                    similarity {
                        # 是否启用?
                        enable=false
                        # 用户名的采样的最大个数
                        max-list=10
                        # 名称有效时间 如果无效 则丢弃. (秒)
                        # 避免玩家多次加入导致封禁玩家
                        valid-time=4
                        # 当字符串相似度达到该值 则不允许他们加入服务器
                        length=4
                    }
                }
                
                # 防火墙缓解
                firewall {
                    # INTERNAL: 内置L7层缓解
                    # SYSTEM: iptables&ipset L4层缓解
                    # INTERNAL_AND_SYSTEM: 两者结合
                    # SYSTEM需要在Linux系统上使用,运行的实例必须有root权限 且已安装iptables&ipset
                    mode=INTERNAL
                    # 临时封禁有效时间 单位为秒. 需重启服务器生效
                    temp-expire-time=30
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
                # 修改此选项需要重启服务器.
                proxycheck-io {
                    enable=false
                    # API秘钥 您需要在上面注册一个账户才可以使用该服务.
                    key="your-key-here"
                    # 单日可查询次数限制. 这取决于你的计划. 但由于我们并不真正保存使用次数 
                    # 而是作为插件在检查时的请求次数-1 以避免无限制调用接口.
                    limit=1000
                    # 检查并踢出vpn 但可能会导致消耗两次查询机会
                    check-vpn=false
                    # 每分钟请求限制.
                    throttle=350
                    # 如果您因为proxies-config中配置的代理而被proxycheck禁止访问 请将其设置为true
                    direct-response=false
                }
                
                # 是否使用来自ip-api提供的检测代理服务
                # 修改此选项需要重启服务器.
                ip-api {
                    enable=true
                }
                
                # GeoIP设置 @MaxMind Database
                country {
                    # MaxMind Database下载许可key. 建议使用自己的许可证秘钥.
                    # https://dev.maxmind.com/geoip/geolite2-free-geolocation-data#accessing-geolite2-free-geolocation-data
                    key="LARAgQo3Fw7W9ZMS"
                    # 模式. WHITELIST(白名单), BLACKLIST(黑名单) 或 DISABLED(禁用)
                    mode=DISABLED
                    # 下载 & 查询超时时间 (ms)
                    time-out=5000
                    # https://dev.maxmind.com/geoip/legacy/codes/iso3166/
                    list=["CN"]
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
