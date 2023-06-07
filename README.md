# Moe! Filter

AkaneField的后续重做版本  
一款功能强大的 兼容性极强的反机器人和实用工具接口.

## 此项目仍然在开发中 欢迎贡献!

但在贡献之前 请确保你添加的功能可以模块化(并且你这么做了)  
  
并且确保你的功能有相当的实用性
例如我们的
[ILogger](https://github.com/CatMoe/MoeFilter/blob/main/src/main/java/catmoe/fallencrystal/moefilter/api/logger/ILogger.kt)
和
[LoggerManager](https://github.com/CatMoe/MoeFilter/blob/main/src/main/java/catmoe/fallencrystal/moefilter/api/logger/LoggerManager.kt)
日志过滤器  
尝试用于解决只能设置一个过滤器的问题 之类的**比较有意义**的东西

> 尽量保持String为空 或在配置中指定 目前配置类还在待办事项内 可能将在两三天后实现落地.


## TODO

---

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
