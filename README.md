# Moe Filter

一款功能强大的 兼容性极强的反机器人和实用工具接口.  

---

## 公告:  

### 临时停止更新公告:  

由于我的一些原因 (主要维护者[@Akkariin](https://github.com/FallenCrystal)) 因有各种各样突如其来的事情而被迫放弃更新  

> 说起来 我还是在这里先讲明了我有心理疾病吧 有些事情还是不在这里说了..  
> 我也不确定我能不能保证v3的更新 如果真的维护不下去了 我可能会考虑转移ownership  
  
我仍然会感谢那些支持/使用本插件的人 以及在这条没有尽头的旅途中遇到的人  
  
以及关于v3计划的一些饼: (虽然是饼 但也会尝试努力实现的啦;w;)  
Velocity&Bungee支持需要考虑重写大部分代码 所以我将其推至v3  
v2我仍然会在Bungee上维护 直到我认为几乎满意了为止  
同时我也将深入Netty的和Minecraft协议学习  
以更好的支持通用的数据包检查以及其它一些东西smth  

还有一些其它东西 例如独立的插件系统...  
v3我会想完善接口之类的代码 让那些插件可以轻松通过MoeFilter在Bungee和Velocity上使用  
我知道这Flag立的有点太.. 但我会尽量的 不可否认Velocity确实算的上Next Gen  
但接口便携远不如Bungee  
  
所以我想创造一个东西 既可以跨平台(实际上就是捏个新平台())  
既可以让插件可以兼容 又可以使用像bungee那样方便直观的接口  
  
> 顺便可以夹带私货(

> 其实一开始MoeFilter其实就是实用工具哦 反机器人是后来添加的  
  
~~顺便骗点人来点Star( 嘛 不过数据什么的还是不太重要 反正都是给自己服务器写的(~~

### 旧:  
  
v1完成了注入管道相关代码并且已经实现了异常检查和First-join PingJoin之类的检查  
感谢那些支持本插件的人, 尽管这在之后会变成付费开源的产物.  

有些抱歉的事情是它不一定兼容全部分叉或插件.  
如果您需要使用的插件设计到修改管道但急需使用那个插件 可以考虑打开issue  
采纳features了之后将会实施 您永远都不需要多余的插件做相同的事情来降低性能.  

## v1进展?:  

抱歉 我目前不能给出任何关于v1 release的[ETA](https://cn.bing.com/search?pglt=169&q=eta%E6%98%AF%E4%BB%80%E4%B9%88%E6%84%8F%E6%80%9D)  

如果您想试试MoeFilter 请自行构建 如果您没有那个能力  
请加入[猫萌的Discord服务器](https://discord.gg/JtXxFNNc9K)然后dm我 `FallenCrystal`  
  
但请注意 请不要将您得到的MoeFilter副本**发送给任何人**.  
因为MoeFilter在之后计划成为[付费开源](https://zhuanlan.zhihu.com/p/162055034) 的产物.  

## 依赖项提示:

此插件现在使用[BungeeKotlinLib](https://github.com/LensMemory/BungeeKotlinLib/)来减少包体积
在[此处下载对应的Kotlin 1.8.22版本的lib](https://github.com/LensMemory/BungeeKotlinLib/releases/tag/1.8.22)

## 正在寻找合适的反机器人?:

好的.. 不要再看了! 全国 #1 的反机器人就在这里  

> 我不管, 我就是要寻找别的替代方案! ?:

您可以在这里查看[其它反机器人](#其它反机器人列表)列表

---

## Features:

 - 对于已被阻止的机器人 不要呼叫PreLoginEvent而保留性能
 - 强大的兼容性 (您可以选择 `修改管道` 或 `传统事件`)
 - 使用管道以获得最佳性能
 - 缓存数据包以避免在大型攻击时造成巨额消耗
 - 可自定义的Ping+Join检查 阻止绝大多数愚蠢的机器人
 - 丰富以及强大的接口
 - 十六进制颜色&渐变 MiniMessage支持
 - 自定义消息占位符设置 轻松创建属于自己的排版 (目前仅限踢出)
 - 不要检查Geyser玩家 `仅支持Geyser-BungeeCord` `Floodgate备选`

> 您考虑过多少个反机器人而它们都将基岩版玩家拒之门外? 现在您不需要担心了!

### 还不够多?

在[此处](https://github.com/CatMoe/MoeFilter/issues/new)打开一个问题或查看我们的[TODO](#TODO)列表

---
## 用前须知/免责声明:

MoeFilter作为一个实用工具**BungeeCord**插件 不会迁移到其它平台 (e.x Velocity)  
  
不会支持任何安装了两个或更多的反机器人插件的BungeeCord 这样做非常愚蠢  
不能保证支持任何BungeeCord分叉 如果出现问题 首先询问BungeeCord分叉的开发者  
  
### 不要报告任何慢速反机器人绕过!:  
#### **100%消除机器人是不可能的事情!**  
没有能绝对能杜绝机器人的东西. 要么牺牲玩家体验 要么就存在有潜在的绕过风险.  
任何反机器人皆在与保护您的服务器不在攻击中倒下 而不是完全消除机器人. 
> 在挨打时自动开白名单的插件**真的**能算是一个**反机器人**插件**吗?**  

在遭到攻击时 您应该庆幸的是在攻击期间您的服务器安然无恙  
而不是在乎那没有脑子却能比玩家还轻松熟练地通过检查的机器人  
  
### Bungee反机器人分叉或其它反机器人插件?:

您可以将MoeFilter放在BungeeCord以及其分叉上 一切都将按预期工作  
但我无法保证您的服务器在遭受机器人攻击时 MoeFilter不会被绕过  
  
> **[!]** 不要添加两个或两个以上的反机器人插件 推荐做法是一个BungeeCord仅运行一个反机器人  
>  *对于MoeFilter而言, 您可以选择自带反机器人的分叉, 有关您可以选择的分叉 请查看 [这里](#完全兼容的bungeecord)*
  
> Tips: 实际上完全兼容的分叉大多数时候它们都不如MoeFilter本身
  
### 我该选择什么缓解攻击的BungeeCord分叉? :  

首先 如果您不想在这上面花钱 您可以选择 [BotFilter](https://github.com/Leymooo/BungeeCord) 或 [Waterfall](https://papermc.io/downloads/waterfall)
  
#### [BotFilter](https://github.com/Leymooo/BungeeCord):  
BotFilter足以降低坏数据包和机器人带来的影响. 因其虚拟服务器验证码而出名  
您可以将其和MoeFilter一起使用 以防止大量机器人淹没BotFilter.  

 > BotFilter应该是目前最合适搭配的分叉. 如果您想试试别的 可以看看下面的列表

### 完全兼容的BungeeCord  
  
  支持修改管道以可以使用MoeFilter所有功能的:
   - [BungeeCord](https://github.com/SpigotMC/BungeeCord)
   - [Waterfall](https://github.com/PaperMC/Waterfall)
   - [BotFilter](https://github.com/Leymooo/BungeeCord)
   - [FlameCord](https://github.com/arkflame/FlameCord)

   不支持管道修改 但可以用`EVENT`模式来支持的BungeeCord的:

  - [BetterBungee](https://github.com/LucazPlays/BetterBungee)  
  - [NullCordX](https://builtbybit.com/resources/nullcordx-lightweight-antibot.22322/)
  
  
  MoeFilter完全支持以上列表中的BungeeCord而不会引发任何兼容性问题(预期行为)  
  在`antibot.conf`中使用`mode: PIPELINE`以获得最佳性能以及全部功能.  

 > 我会尽力兼容大多数BungeeCord分叉 因此如果您还想要更多的选择 请随时回来查看页面是否更新!

 > 如果您想要让MoeFilter支持开放源代码的BungeeCord分叉 请在[此处](https://github.com/PaperMC/Waterfall)打开一个issue.

### 其它反机器人列表:  
  
> 此列表皆在列出那些比较出名的反机器人:

  - [nAntiBot](https://en.docs.nickuc.com/v/nantibot/about)
       - nAntiBot确实是一个很好的反机器人 但由于依赖云服务 对中国用户不是很友好
       - 如果您的服务器可以很好的访问它的云服务 您确实可以使用它而不是MoeFilter
  - AntiAttackRL (链接已死)
       - 曾经陪伴过各个服主的~~反机器人~~ 自动白名单插件
       - 尽管它尽责了 但它仍然不是很值得使用 (读下面看为什么)
       - 不必要的控制台垃圾邮件. 应该只保留必要信息以节省CPU使用率
       - 运用多个事件侦听器来完成检查
       - 不必要的反射注入管道 (也可能没写完? 但至少目前不值得使用 且已经宣布永久停更)
       - 自己的AlreadyOnline检查 甚至对于Collection<ProxiedPlayer>不是使用.contains  
         而是使用for循环遍历所有在线玩家 如果.equals再cancelled事件
         加入的一多这for循环就经不起折腾了.
       - 已被废弃的半成品
       - 每个不同的检查使用独立的事件 一个加入检查触发三个PreLoginEvent监听器
  - [BotSentry](https://www.spigotmc.org/resources/%E2%9A%A1-botsentry-%E2%9A%A1-antibot-antiproxy-resisting-30k-bots-per-second-bungee-spigot-sponge-velocity.55924/)
       - "您已被列入黑名单  如果您认为您不是机器人 请点击下面的链接进行认证"
       - 仅依靠反代理云服务和 FirstJoin检查的狗屎
       - 新版本不如旧版本 资源链接下方的视频都是老视频
       - 如果遭到大型攻击 则您的服务器会立马崩溃
       - 这边注入Netty管道handle LoginRequest数据包 那边监听PreLoginEvent 双倍伤害
  - [Sonar](https://github.com/jonesdevelopment/sonar)
       - 我目前见过最好的开源反机器人插件
       - 但.. 它多少有些囊肿了
       - 倒也不是不可以用
  - [EpicGuard](https://github.com/4drian3d/EpicGuard)
       - 可以很好地防御大部分攻击 尽管它也只依靠事件
       - 尽管不是很具有高性能 但它也可以是好的选择 我的意思是 相比于其它反机器人来讲
  - [Ultimate AntiBot](https://github.com/Kr1S-D/UltimateAntibotRecoded)
       - 万能的PreLoginEvent 在上面实现所有反机器人逻辑
       - 愚蠢的Lockdown策略 结合反VPN的自动白名单插件 但具有没用的自动过审功能
       - 给自己服务器用嘛.. 还开源 不寒碜
       - 很多人正在使用它 尽管在新版本也会变成过度依赖云服务的垃圾
   - [JH AntiBot v4](https://www.jhdev.net/details.php?id=7)
       - 还是写给自己服务器的东西.. (相比于UAB 很多东西是硬编码的)
       - 是一个~~反机器人~~ 每秒链接计数器
       - 需要爬取到代理才能让玩家进服务器 但相比起在攻击时调用API来检查来讲 算是好得多
       - 消失的v5版本
       - ~~它作为MCStorm的测试插件 不是因为JH Antibot是万能的 而是Jheyson曾是MCStorm工作人员.~~
   - [XProtect](https://github.com/LagBug/XProtect)
       - 它甚至不是BungeeCord插件 我只是在此告诉你这个而已 (xD)
       - ~~因为我在之前还是个无知的孩子的时候买了那个插件 声明支持BungeeCord~~
         ~~但实际上它的意思是将XProtect放在后端Spigot服务端 妥妥智商税~~


> **[?]** 我不会对任何反机器人做出好坏的评价 请根据您自己的测试来平衡  

> **[!]** 无论如何 不要以"能防住就行"的概念来草率地决定您的服务器应该使用什么反机器人  
> **[!]** 如果您不知道如何权重反机器人的好坏 那请将保留CPU使用率为第一目的.

  
### 想为反机器人模块做出贡献? :

首先 对攻击的方式以及反机器人的原理有充足地理解再做出贡献. 我不倡导直接实现天马行空的想法.

  - 使用ConnectionUtil切断连接 如果需要带上理由 请使用FastDisconnect  
    ```
    ConnectionUtil(pendingConnection).close
    ```
    ```
    FastDisconnect.disconnect(channel, DisconnectType)
    FastDisconnect.disconnect(connectionUtil, DisconnectType)
    ```
    不会支持自定义理由. 如果您在玩家已经连接到时想踢出 请自己写一个插件来完成  
    并且您可以有无数种办法将玩家踢出服务器或切断它们的连接.
    
  - 使用缓存来完成大部分操作. 但对于部分使用缓存用处不大的地方 可以考虑直接存储  

  - 反机器人不是反崩溃

---

## 此项目仍然在开发中 欢迎贡献!

但在贡献之前 请确保您添加的功能可以模块化(并且你这么做了)  
  
并且确保您的功能有相当的实用性 也请避免在一块地方复制同样的代码
尝试用于解决只能设置一个过滤器的问题 之类的**比较有意义**的东西

> 对于非调试目的的代码, 尽量在配置文件中写明配置  
> 对于某些拥有独立标志性的代码 可以选择硬编码String. 例如命令

---

## TODO


  - [x] 日志接口
  - [x] 命令接口
  - [x] 事件接口
  - [ ] 事件接口v2
  - [x] HOCON配置
  - [x] MiniMessage 支持 
  - [x] 基本反机器人检查 (Ping, FirstJoin, etc)
  - [x] 踢出消息缓存
  - [x] 管道注入和支持
  - [x] 基于事件的反机器人 (对于不兼容的插件或BungeeCord)
  - [x] 热加载
  - [ ] 清理代码 ([#24](https://github.com/CatMoe/MoeFilter/issues/24))
  - [x] 纯数据包消息发送器 (为了更好的性能! [#25](https://github.com/CatMoe/MoeFilter/issues/25))
  - [ ] Limbo机器人过滤
  - [ ] ~~为了更好而变得更好!~~

> 这只是**临时的** TODO列表 会随着时间的推移而增加 不代表整个项目的process.

---

## 学分

---

 项目含储存库中借鉴的代码 没有它们 就没有MoeFilter:

  - [BotFilter (BungeeCord)](https://github.com/Leymooo/BungeeCord)
  - [Velocity](https://github.com/PaperMC/Velocity)
  - [Sonar](https://github.com/jonesdevelopment/sonar)

---

Made by CatMoe.
