# Moe Filter

一款功能强大的 兼容性极强的反机器人和实用工具接口.

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
  
您可以考虑与[BotFilter](https://github.com/Leymooo/BungeeCord)搭配使用 但BotFilter并不支持Geyser  
如果您使用Geyser 您可以考虑购买/构建[FlameCord](https://github.com/arkflame/FlameCord)  
  
 > [!] 如果您要使用FlameCord 请记住 FlameCord应该**只作为**后盾  
 > [!] 来避免遭遇MoeFilter无法缓解的无效数据包攻击  

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
