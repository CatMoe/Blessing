# Moe Filter

一款功能强大的 兼容性极强的反机器人和实用工具接口.  

---

## 公告 (停更通知):

我目前暂无精力编写反机器人模块 如果更新了实用工具 那也可能是仅我需要而已.  
如果您有兴趣维护该项目 您可以将拉取请求推送至[v1](https://github.com/CatMoe/MoeFilter/tree/v1)分支, 谢谢.  
  
API v6以及之后的版本已经可以抵御来自MCStorm的攻击 且配备有自己的Throttle 足够对大型攻击进行一定的缓解.  
我也可能会随时决定是否在之后的编码中修改BungeeCord本身来达到效率最大化  
因为我目前无法看到在未经专门修改BungeeCord的单纯依靠事件的反机器人的前途.  

---
## 用前须知/免责声明:

MoeFilter作为一个实用工具**BungeeCord**插件:
  - 不会迁移到其它平台 (e.x Velocity)
  - (可能)不会将反机器人放在第一位
  - (可能)不会接受大多数改良反机器人模块的建议

不会支持任何安装了两个或更多的反机器人插件的BungeeCord 这样做非常愚蠢  
不能保证支持任何BungeeCord分叉 如果出现问题 首先询问BungeeCord分叉的开发者  
  
### 那反机器人呢? 我用MoeFilter就是为了它的反机器人的! :

您可以将MoeFilter放在BungeeCord以及其分叉上 一切都将按预期工作  
但我无法保证您的服务器在遭受机器人攻击时 MoeFilter不会被绕过
  
> **[!]** 不要添加两个或两个以上的反机器人插件 推荐做法是一个自带反机器人的BungeeCord分叉和一个反机器人  
> **[!]** 不过 我无法保证所有反机器人都可以跟自带缓解攻击的BungeeCord分叉一起使用 例如[nAntiBot](https://en.docs.nickuc.com/v/nantibot/about)  
> **[!]** 至少对于MoeFilter来讲 这么做是最合适的  
  
### 我该选择什么缓解攻击的BungeeCord分叉? :  

首先 如果您不想在这上面花钱 您可以选择 [BotFilter](https://github.com/Leymooo/BungeeCord) 或 [Waterfall](https://papermc.io/downloads/waterfall)
  
#### [FlameCord](https://github.com/arkflame/FlameCord):  
MoeFilter实际上比FlameCord更好 但如果您想要GeoIP IPTables&IPSet 和坏数据包防御  
考虑到它的价格和开放源代码 您可以使用它跟MoeFilter一起使用.  
> 记得关闭除GeoIP和Exception数据包防御之外的功能 MoeFilter比它做的更好  

> MoeFilter 在之后会加入IPTables & IPSet支持
  
#### [BotFilter](https://github.com/Leymooo/BungeeCord):  
BotFilter足以降低坏数据包和机器人带来的影响. 因其虚拟服务器验证码而出名  
您可以将其和MoeFilter一起使用 以防止大量机器人淹没BotFilter.  
  
> 更多BungeeCord分叉将会根据我的使用体验在之后列出 当然我也可能不会再考虑更新该页面:

### 其它反机器人列表:  
  
> 此列表皆在列出那些比较出名的反机器人:

  - [nAntiBot](https://en.docs.nickuc.com/v/nantibot/about)
       - nAntiBot确实是一个很好的反机器人 但由于依赖云服务 对中国用户不是很友好
       - 如果您的服务器可以很好的访问它的云服务 您确实可以使用它而不是MoeFilter
  - AntiAttackRL (链接已死)
       - 曾经陪伴过各个服主的~~反机器人~~ 自动白名单插件
       - 尽管它尽责了 但它仍然不是很值得使用 (读下面看为什么)
       - 不必要的控制台垃圾邮件. 应该只保留必要信息以节省CPU使用率
       - 在Handshake阶段总是注入AbstractWrapper 可以被大力出奇迹.
  - [BotSentry](https://www.spigotmc.org/resources/%E2%9A%A1-botsentry-%E2%9A%A1-antibot-antiproxy-resisting-30k-bots-per-second-bungee-spigot-sponge-velocity.55924/)
       - "您已被列入黑名单  如果您认为您不是机器人 请点击下面的链接进行认证"
       - 仅依靠反代理云服务和 FirstJoin检查的狗屎
       - 新版本不如旧版本 资源链接下方的视频都是老视频
       - 如果遭到大型攻击 则您的服务器会立马崩溃
       - 源代码也已经被混淆 但我相信它跟AntiAttackRL那样在Handshake阶段干了相同的事情
  - [Sonar](https://github.com/jonesdevelopment/sonar)
       - 我目前见过最好的开源反机器人插件
       - 但.. 它多少有些囊肿了
       - 倒也不是不可以用
  - [EpicGuard](https://github.com/4drian3d/EpicGuard)
       - 可以很好地防御大部分攻击
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
   - [XProtect](https://github.com/LagBug/XProtect)
       - 它甚至不是BungeeCord插件 我只是在此告诉你这个而已 (xD)


> **[?]** 我不会对任何反机器人做出好坏的评价 请根据您自己的测试来平衡  

> **[!]** 无论如何 不要以"能防住就行"的概念来草率地决定您的服务器应该使用什么反机器人  
> **[!]** 如果您不知道如何权重反机器人的好坏 那请将保留CPU使用率为第一目的.

  
### 想为反机器人模块做出贡献? :

首先 通过直接修改BungeeCord的class来侦听监听管道(或类似的侵入性做法)不会得到支持.

支持使用事件的组合来创建反机器人:
  
  - 对于无效数据包或者玩家一般不会触发到的检查  
    快速使用PendingConnection或防火墙切断连接  
      
  - Ping检查应该在Handshake上检查. `requestProtocol == 1`  
    而不是使用ProxyPingEvent (这也可以保证兼容自带MOTD的BungeeCord分叉)  
    
  - 优先使用缓存而不是单独的Map或玩家来缓存某些内容  
    也请做好在单IP上连接数过大而未被及时被防火墙拦截的连接检查做好准备  
      
  - 在Handshake注入AbstractWrapper是愚蠢且不必要的 这么做反而会增加CPU使用率  
    对于数据包限制器或者反崩溃类似的东西 使用其它插件而不是MoeFilter
    
  - 对攻击的方式以及反机器人的原理有充足的理解再做出贡献
      
  - 不要注入Netty管道并造成需要处理双倍的InitialHandler
  
如果您或您的提交满足以上条件之后 欢迎对MoeFilter做出贡献.

---

## 此项目仍然在开发中 欢迎贡献!

但在贡献之前 请确保您添加的功能可以模块化(并且你这么做了)  
  
并且确保您的功能有相当的实用性
例如我们的
[ILogger](https://github.com/CatMoe/MoeFilter/blob/main/src/main/java/catmoe/fallencrystal/moefilter/api/logger/ILogger.kt)
和
[LoggerManager](https://github.com/CatMoe/MoeFilter/blob/main/src/main/java/catmoe/fallencrystal/moefilter/api/logger/LoggerManager.kt)
日志过滤器  
尝试用于解决只能设置一个过滤器的问题 之类的**比较有意义**的东西

> 尽量保持String为空 或在配置中指定 目前配置类还在待办事项内 可能将在两三天后实现落地.

---

## TODO


  - [x] 日志接口
  - [x] 命令接口
  - [x] 事件接口
  - [x] HOCON配置
  - [x] MiniMessage 支持 
  - [ ] 基本反机器人检查 (Ping, FirstJoin, etc)
  - [ ] 踢出消息缓存
  - [ ] ~~当切换版本时 自动补全配置文件中缺失的内容.~~ 
 > 遥遥无期 并且我可能并没有想这么做. 最简单的办法是备份他们的配置文件 然后生成一份新的)

> 这只是**临时的** TODO列表 会随着时间的推移而增加 不代表整个项目的process.

---

Made by CatMoe.
