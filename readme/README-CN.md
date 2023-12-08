## 关于

MoeFilter是专门为BungeeCord制作的反机器人以及实用工具. 
可以在大多数分叉上运行. (例如[Waterfall](https://github.com/PaperMC/Waterfall/))

## 功能 ✨

> 反机器人 / 反压测
- 通过Geyser和Floodgate连接的玩家不需要通过反机器人检查.
- 不需要修改代理的字节码文件, 以便兼容其它分叉.
- 不需要依赖第三方服务, 可以在网络不好时工作.
- 不需要呼叫BungeeCord上的`PreLoginEvent`事件, 无需担心其它插件注册监听器带来的性能损耗.
- 大多数反机器人模块都是可配置的.
- 没有白名单模块或类似的东西, 保证在攻击期间玩家也能正常加入.
- 独立的虚拟服务器, 以帮助检查玩家的客户端并过滤垃圾邮件和机器人攻击.
- 数据包顺序检查, 防止不标准的Minecraft协议连接到服务器.
- 以及其它反机器人几乎都有的检查, 例如用户名有效性检查, IP地理位置检查, 以及更多..
> 翻译器 / 通用平台
- 不需要编写冗余的判断逻辑. 异步执行命令, 是否允许控制台执行命令等等 ——只需要在注解中声明它.
- [`PlayerPostBrandEvent`](https://github.com/CatMoe/MoeFilter/blob/stray/translation/src/main/kotlin/catmoe/fallencrystal/translation/event/events/player/PlayerPostBrandEvent.kt)事件. 当玩家
- 你可以使用它来编写同时兼容 [Velocity](https://github.com/PaperMC/Velocity) 和 [BungeeCord](https://github.com/SpigotMC/BungeeCord) 两个平台的代码.
- 所有关于聊天的功能都考虑到了 [MiniMessage](https://docs.advntr.dev/minimessage/).


## 兼容性 🔧

> ⚠️ **Velocity版本只负责驱动MoeTranslation**
> 反机器人功能不会移植到其它平台.
> [了解更多](https://github.com/CatMoe/MoeFilter/tree/stray/readme/postscript)

| BungeeCord或其分叉 | 支持 |
|----------------|----|
| BungeeCord     | ✅  |
| Waterfall      | ✅  |
| FlameCord      | ✅  |
| BotFilter      | ✅  |
| BetterBungee   | 🛑 |
| Travertine     | ❔  |
| XCord          | ❔  |
| NullCordX      | ☑️ |

| Plugins         | Support |
|-----------------|---------|
| nAntiBot        | 🛑      |
| HAProxyDetector | ❔       |
| JH AntiBot      | 🛑      |
| Protocolize     | ✅       |
| JPremium        | ☑️      |
| ViaVersion *    | 🛑      |
| Triton          | ☑️      |

>✅: 
>完全支持,
>跟MoeFilter一起使用也不会引发兼容性问题  
>☑️: 
>支持,
>但是插件的某些功能可能会不工作或引发兼容性问题.  
>❔: 
>未测试,
>但它应该可以跟这些软件一起使用.  
>🛑: 
>不受支持,
>对于使用这些软件导致的异常我们概不负责.

>如果您对某个软件的兼容性有疑问, 
>或想问关于为什么不兼容的细节.
>请在 
>[这里](https://github.com/CatMoe/MoeFilter/issues)
>打开一个问题,
>或尝试在
>[这里](https://github.com/CatMoe/MoeFilter/issues/56)
>找到答案.

> *: 
> ViaVersion是支持的, 但仅限于您安装在后端服务器而不是代理上.
> [了解更多](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714924303)

## 📷 预览

> 这里目前什么都没有. 请稍后再来