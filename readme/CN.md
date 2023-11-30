<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
  <div class="icon-container">
    <img src="https://i.postimg.cc/fLTbc7VB/ba-icon.png" alt="ba-icon">
  </div>
  <div class="shields">
    <img src="https://img.shields.io/github/languages/code-size/CatMoe/MoeFilter?style=flat-square" alt="Code-Size">
    <img src="https://img.shields.io/github/v/release/FloraCore/FloraCore?style=flat-square" alt="Release">
    <img src="https://img.shields.io/github/license/CatMoe/MoeFilter?style=plastic" alt="License">
  </div>
</div>


# Moe Filter

⚡ 强大的,为BungeeCord设计的高性能实用工具兼反机器人. (Minecraft 1.8.x-1.20.2)

---

## ✨ 功能/特点:
### 对于反机器人:  
  - 通过Geyser连接的玩家将跳过检查直接加入服务器 `仅限Geyser-BungeeCord和Floodgate!`
  - 可配置的 Ping & Join 检查 阻止绝大多数机器人
  - 预缓存踢出数据包 在遭受攻击时可以保留CPU使用率 并显著降低切断连接所需要的时间
  - 不要为被踢出服务器的客户端呼叫`PreLoginEvent`
  - 干净的控制台. 甚至不需要控制台过滤器来做到这点. `仅限开启Limbo的状态下`
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

---

## 🔧 支持的 BungeeCord forks / 插件:  

> **不支持除了BungeeCord之外的平台. Velocity版本仅为MoeTranslation提供支持**

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
| ViaVersion      | 🛑 |
| Triton          | ☑️ |

有用的链接:
- [如何让MoeFilter兼容JPremium](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714907598)
- [不要在代理上安装ViaVersion / ProtocolSupport](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714924303)

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

此储存库含有来自以下储存库或网站的内容
 - [BungeeCord](https://github.com/SpigotMC/BungeeCord)
 - [Velocity](https://github.com/PaperMC/Velocity) `一些解码器之类的参考`
 - [Sonar](https://github.com/jonesdevelopment/sonar) `劫持 SERVER_CHILD 以及初始化管道的反射代码`
 - [EpicGuard](https://github.com/awumii/EpicGuard) `GeoIP的初始化以及检查`
 - [NanoLimbo](https://github.com/Nan1t/NanoLimbo) `基础的Limbo服务器`
 - [BotFilter](https://github.com/Leymooo/BungeeCord) `群戏, 群戏数据包等`
 - [wiki.vg](https://wiki.vg/Protocol) `1.8.x至最新的Minecraft版本的协议支持.`
 - [NullCordX](https://builtbybit.com/resources/nullcordx-lightweight-antibot.22322/) `代理列表提供者`
 - [FlameCord](https://builtbybit.com/resources/flamecord-the-ultimate-antibot.13492/) `代理列表提供者`
 - [PacketLimiter](https://github.com/Spottedleaf/PacketLimiter) `数据包限制参考`

感谢上述所有项目帮助我们更好地构建这些东西.

---

#### 其它东西:

[附言](https://github.com/CatMoe/MoeFilter/blob/stray/readme/postscript/postscript-CN.md)  
[旧的readme](https://github.com/CatMoe/MoeFilter/blob/stray/readme/legacy.md)

---
