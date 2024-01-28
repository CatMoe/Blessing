# Fallback 模块

Fallback服务器模块是一个集合了
[nbt工具模块](https://github.com/CatMoe/Blessing/tree/recoded/nbt)
和
[协议模块](https://github.com/CatMoe/Blessing/tree/recoded/protocol)
的, 用于启动虚拟服务器的模块.

这样的虚拟服务器也会被某些人称作"Limbo"

> ⚠️ Fallback模块**不是拖拽即用的应用程序**
> 它皆在帮助开发人员轻松创建自己的虚拟服务器
> 以集合到自己的应用程序上.

> ⚠️ 要使用Fallback 您必须拥有netty开发经验 
> 并基本熟知Minecraft协议的工作原理.

> ✅ 如果您想 您可以在任何地方注入Fallback,
> 无论是在什么代理上 甚至是自己搭建Bootstrap启动.

Fallback没有太多东西. 它是一个完全空的服务器
也没有设置方块的能力 (取决于协议模块何时支持)

目前还没有太多api. 但已经有足够多的可自定义性.

---

# FallbackSettings

[FallbackSettings](https://github.com/CatMoe/Blessing/blob/recoded/fallback/src/main/kotlin/net/miaomoe/blessing/fallback/config/FallbackSettings.java)
是一个类. 它通过您给予的预设来决定fallback要干什么

> 🔧 FallbackSettings拥有一个javadoc 
> 该文中不一定包含了最新的可以设置的选项
> 如果您能理解英文 那么可以选择查看doc.

> ✅ 您不一定只能通过FallbackSettings来决定fallback该干什么. 
> 您可以在监听器中访问
> [FallbackHandler](https://github.com/CatMoe/Blessing/blob/recoded/fallback/src/main/kotlin/net/miaomoe/blessing/fallback/handler/FallbackHandler.kt)
> 来获取某些内容 例如玩家所在的位置 它们的客户端标签以及更多.
> 也可以通过`FallbackHandler#write`来写入数据包.

> ❔对于自定义收发数据包, 请自己实现处理器. 例如`ChannelDuplexHandler`.

---

## 配置fallback

FallbackSettings的构建参数是私有的. 
但您可以通过静态的`create()`方法新建一个.

> ⚠️在某些情况下动态修改设置可能不可用
> 这是由于数据包已经被缓存造成的.
> 如果真的有这样的需求 请使用`setUseCache(false);`

可用设置:

### World

该选项用于让fallback告诉客户端它们处在什么世界/维度 (默认为主世界)

可用选项:

- `World.OVERWORLD` (主世界)
- `World.THE_NETHER` (地狱)
- `World.THE_END` (末地)

### Brand

fallback应该向客户端发送什么服务端标签.
该选项可以在f3调试界面的左上角看到

默认标签为`Blessing`.

> ✅ 
> DefaultMotdHandler
> 也会在您没有自定义Motd处理器的时候使用该项. 
> 有关更多信息 请查看[这里](#motdhandler)
> 

支持MiniMessage 但是目前仅支持非十六进制颜色. 
这是由于原版限制引起的.

### playerName

该配置用于告诉客户端它们(在服务器里)叫什么名字.
由于需要使用数据包缓存 所以不支持玩家本身的名称.

默认名称为`Blessing`.

### validate

是否启用数据包有效性检查 (默认启用)

该检查可以一定程度上防止客户端滥用数据包并切断它们的连接,

> ⚠️该检查**不是为了检查机器人而设计的** 对于机器人检查
> 请自己实现检查.

### timeout

在没有任何数据包传入多少毫秒后自动切断连接

### motdHandler

如果客户端正在尝试向fallback请求状态
我们应该从什么处理器那获取Motd并返回?

接受一个
[FallbackMotdHandler](https://github.com/CatMoe/Blessing/blob/recoded/fallback/src/main/kotlin/net/miaomoe/blessing/fallback/handler/motd/FallbackMotdHandler.kt)

> 🔧 默认情况下 将会使用
> [DefaultMotdHandler](https://github.com/CatMoe/Blessing/blob/recoded/fallback/src/main/kotlin/net/miaomoe/blessing/fallback/handler/motd/FallbackMotdHandler.kt)
> 作为没有实现MotdHandler的替代品.  
> 它将返回一个亮紫色的"Blessing <3".  
> 如果fallback/protocol支持目标客户端版本 则会返回相同协议号的Motd
> 否则返回-1
> 标签也将是设置中指定的brand.  
> 在线玩家和最大玩家数始终为0.

### exceptionHandler

用于捕获到达FallbackHandler的异常.  
如果为空 则将主动切断连接.

> 🔧无论exceptionHandler是否被实现 
> 异常也会在[debugLogger](#debuglogger)中被打印.

> ⚠️ 当您设置了ExceptionHandler时 连接并不会被fallback主动断开.
> 如果您需要切断连接 请自行添加代码`ctx.close();`.

### initListener

当FallbackInitializer的initChannel被触发时
触发该函数.

> ❔它要求一个BiConsumer<FallbackHandler, Channel>. 最简单的示例可能是
> ```java
> import io.netty.channel.Channel; 
> import net.miaomoe.blessing.fallback.config.FallbackSettings;
> import net.miaomoe.blessing.fallback.handler.FallbackHandler;
> 
> class SimpleFallbackImpl {
>     public final FallbackSettings settings = FallbackSettings
>         .create()
>         .setInitListener(this::onConnecting);
>         // TODO setting another option or build initializer
> 
>     private void onConnecting(final FallbackHandler handler, final Channel channel) {
>         // TODO when connecting
>     }
> 
> }
> ```

### debugLogger

设置用于打印调试日志的记录器

> ✅ 默认为空. 空则代表不打印任何调试日志

### spawnPosition

设置玩家的默认重生点 (与joinPosition不同!)

默认为 `Position(7.5, 100, 7.5)`

> ❔该字段是如此无用, 
> 玩家大概率不会在fallback中重生 更不需要设置重生点.
> 但它确实存在.

> ❔即使Position接受的是一个双精度浮点 (Double), 
> 但在实际中它将会被转换成Int.

### joinPosition

玩家加入fallback时重生的位置.  
默认为 `x 7.5, y 100, z 7.5, yaw 180f, pitch 0f, onGround false`

> ❔onGround始终为false 虽然1.7客户端仍然接受该值.

### teleportId

在传送玩家时的传送id. 用于确认1.9+客户端是否接受传送.  
默认值为`7890`

> ❔尽管它可以用于检查客户端是否有效 
> 但不确定是否会因为自定义发包而造成错误.

### useCache

是否使用缓存 (默认为true)

> ⚠️除非出现不使用缓存才能解决的问题 
> 或有动态变更设置的需求 
> 否则不应该禁用缓存.

> 🔧涉及到有关数据包的(例如disableFall) 
> 在创建缓存时就已经被决定 
> 除非重新创建缓存 否则无法修改.

### disableFall

是否将玩家滞空于空中? (无法移动)  
如果只是将玩家放在fallback而不需要处理坐标 
那么此项可能有用.

### defaultState

Fallback初始化的默认状态 (默认为等待握手)  
对于处理已经处于非握手状态下的连接可能非常有用

> ⚠️在您没有特殊需求之前 不要滥用此项. 
> 可能会干涉到[逻辑处理](#processlogic) 
> 请务必考虑兼容性等等.

### processLogic

是否让fallback自动处理所有必要的逻辑?

例如:  
当玩家尝试向fallback请求状态时 fallback将从motdHandler中请求并返回motd.  
当玩家尝试加入fallback时, 如果协议上支持该客户端 
fallback会自动处理并回应所有(从握手到加入游戏的所需)数据包.

> ⚠️ 如果您需要手动切换fallback状态 请使用 `FallbackHandler#updateState(State)`

> ⚠️ 不要在未完全加入游戏之前干涉逻辑 或者请完全禁用逻辑处理并手动处理所有所需的数据包.

---

## 生效配置

FallbackSettings使用的是链式Setter.

当您已经准备好让fallback接入连接时  
您可以使用`.buildInitializer()`来获取`ChannelInitializer<Channel>`.

例子:

```java
import net.miaomoe.blessing.fallback.config.FallbackSettings;
import net.miaomoe.blessing.fallback.handler.FallbackInitializer;

class Example {

    public final FallbackInitializer initializer = FallbackSettings
            .create()
            .setDisableFall(true)
            .setBrand("Example")
            .setValidate(false)
            .buildInitializer();

}
```

然后使用initChannel方法. 您就可以让fallback处理连接.

---