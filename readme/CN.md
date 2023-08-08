# Moe Filter

⚡ 强大的,为BungeeCord设计的高性能实用工具兼反机器人. (Minecraft 1.7.6-1.20.1)

--- 

### ⚠️ 依赖项警告:  

需要在您的服务器上安装[BungeeKotlinLib](https://github.com/LensMemory/BungeeKotlinLib).  
如果没有它, MoeFilter将不会在您的服务器上按预期(或触发各种各样的异常) 
  
MoeFilter现在使用Kotlin的1.9.0版本 您可以[点这里](https://github.com/LensMemory/BungeeKotlinLib/releases/download/1.9.0/BungeeKotlinLib-1.9.0.jar)来下载此依赖项

---

## ✨ 功能/特点:
### 对于反机器人:  
  - 通过Geyser连接的玩家将跳过检查直接加入服务器 `仅限Geyser-BungeeCord!`
  - 可配置的 Ping & Join 检查 阻止绝大多数机器人
  - 预缓存踢出数据包 在遭受攻击时可以保留CPU使用率 并显著降低切断连接所需要的时间
  - 不要为被踢出服务器的客户端呼叫`PreLoginEvent`
  - 干净的控制台. 甚至不需要控制台过滤器来做到这点.
  - 您可以自定义所有检查模块的工作方式
  - 使用专门的虚拟服务器来防止机器人/攻击绕过或过载代理

  已实施的检查:
   - Ping & Join 组合检查
   - 玩家名称检查 (避免无效或机器人用户名)
   - 连接域检查
   - 地理位置(GeoIP)检查
   - 已在线检查 (避免迫使让已在线玩家退出服务器以绕过白名单)
   - 相似名称检查 (Beta)
   - 反代理检查 (内置爬虫 + 第三方接口)
   - 在虚拟服务器里进行的掉落检查

> 最终效果取决于您在 `antibot.conf` 中配置的工作模式

### 对于插件本身:  
  - 缓存消息数据包
     - 可以显著降低 着色复杂的消息 / 重复发送 / 发送给多位玩家 时的处理器及内存使用率
  - MiniMessage支持
     - 在任何地方使用 十六进制 / 渐变 颜色!
     - (十六进制颜色不适用于低于1.16.x的客户端 以及部分BungeeCord的控制台)
  - 尽可能地使用异步
     - 至于为什么不是full-async ——因为很多时候根本不值得使用异步. 且目前不需要线程调度程序
     - BungeeCord永远不会因为MoeFilter内部的处理而滞后/冻结服务器
  - 兼顾性能以及兼容性
     - 还有什么MoeFilter不兼容的东西吗?
     - 如果您真的遇到了不兼容的东西,你应该去打开一个[issue](https://github.com/CatMoe/MoeFilter/issues)
  - 方便的接口. 你的下一个BungeeCord实用工具插件
     - 嘿! 请不要在花里胡哨的BungeeCord fork上浪费您宝贵的金钱  

---

## 🔧 支持的 BungeeCord forks / 插件:  

> **将在(很久)之后添加对Velocity的支持. 不支持所有Spigot版本&分叉**

| BungeeCord & forks | 支持 |
|--------------------|----|
| BungeeCord         | ✅  |
| Waterfall          | ✅  |
| Flame/MongoCord    | ✅  |
| BotFilter          | ✅  |
| BetterBungee       | 🛑 |
| Travertine         | ❔  |
| XCord              | ❔  |
| NullCordX          | ☑️ |

| 插件              | 支持 |
|-----------------|----|
| nAntiBot        | 🛑 |
| HAProxyDetector | ❔  |
| JH AntiBot      | 🛑 |
| Protocolize     | ✅  |
| JPremium        | ☑️ |

> ✅: 完全支持. MoeFilter可以与其一起使用而不会出现任何问题 (预期行为)

> ☑️: 支持. 但可能会出现非预期行为的兼容性错误

> ❔: 未测试 但应该可以正常工作

> 🛑: 不支持/不受欢迎 我不会对使用已经声明了"不支持"的插件的使用者提供任何兼容性的帮助/支持/修复

---

## 📷 预览

> 这里目前还什么都没有!

如果你制作了一个preview视频并想挂在此处, 请在Discord上dm我 `FallenCrystal`

---

## 📚 学分

此储存库借用了以下储存库的代码
 - [BungeeCord](https://github.com/SpigotMC/BungeeCord)
 - [Velocity](https://github.com/PaperMC/Velocity) `一些解码器之类的参考`
 - [Sonar](https://github.com/jonesdevelopment/sonar) `劫持 SERVER_CHILD 以及初始化管道的反射代码`
 - [EpicGuard](https://github.com/awumii/EpicGuard) `GeoIP的初始化以及检查`
 - [NanoLimbo](https://github.com/Nan1t/NanoLimbo) `基础的Limbo服务器`
 - [BotFilter](https://github.com/Leymooo/BungeeCord) `群戏, 群戏数据包等`
 - [wiki.vg](https://wiki.vg/Protocol) `1.7.6至最新的Minecraft版本的协议支持.`

---

#### 我想看旧的readme!

[点这里](https://github.com/CatMoe/MoeFilter/blob/stray/readme/legacy.md)

---
