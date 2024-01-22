# Protocol 模块

> ⚠️These wikis are currently written in Chinese. 
> If needed. Please use a translator. 
> These may later be migrated to dedicated wiki pages and translated into English.

> ⚠️ 在您开始使用数据包制作魔法之前, 请先了解Minecraft协议是如何工作的.
> 有关Minecraft: Java Edition协议的信息  可以在[wiki.vg](https://wiki.vg/Protocol)中找到.

> ⚠️ 支持的协议是1.7.6-1.20.3/4. 可能会在后续支持1.7.2. 不支持任何1.6及以下的版本

> ⚠️ 此协议仅面对Minecraft: Java Edition. 包括Blessing也是. 对于Bedrock Edition的协议 请寻找其它项目.

---

# 状态选择:

Minecraft协议中有不同的状态 不同的状态对应着不同的数据包. 
([State.java](https://github.com/CatMoe/Blessing/blob/recoded/protocol/src/main/kotlin/net/miaomoe/blessing/protocol/registry/State.java))

目前已经支持的状态:
  - 握手 (Handshake)
  - 状态 (Status)
  - 登录 (Login)
  - 配置 (Configuration) (1.20.3+)
  - 游玩 (Play)

> ❔对于您该选择哪个状态 请先了解Minecraft的协议中的状态.

> ✅ 对于需要创建虚拟服务器的应用程序来说, 
> [fallback](https://github.com/CatMoe/Blessing/tree/recoded/fallback)
> 模块可能对您来说更有用
> 即使您不想使用它 其中的一些思路也可能值得供您参考.

> ⚠️ 您可能需要在启动时就初始化所需的状态. 
> 避免在调用枚举时才进行注册 导致应用程序初次处理缓慢.

---

# 方向:

当您确定了状态之后 您还要选择您需要的数据包方向.
  - client bound : 服务器发送 客户端接受 也就是server to client (s2c)
  - server bound: 服务端接受 客户端发送 也就是client to server (c2s)

某些数据包可能具有双向性 (即同时支持client和server bound 例如
[KeepAlive](https://github.com/CatMoe/Blessing/blob/recoded/protocol/src/main/kotlin/net/miaomoe/blessing/protocol/packet/common/PacketKeepAlive.kt))

确认了状态和方向之后 您就能得到一个
[ProtocolMappings](https://github.com/CatMoe/Blessing/blob/recoded/protocol/src/main/kotlin/net/miaomoe/blessing/protocol/mappings/ProtocolMappings.kt)
对象.

例如:

```java
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings;
import net.miaomoe.blessing.protocol.registry.State;

class Example {
    public final ProtocolMappings mappings = State.PLAY.clientbound.getValue();
}
```

---

# 获取数据包

您可以通过数据包ID或数据包的类来获取数据包本身.

> ⚠️ 如果没有找到对应的数据包 则会抛出NullPointerException异常 
> 请确保正确处理在获取失败时发生的异常.

## 通过数据包ID:

```java
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings;
import net.miaomoe.blessing.protocol.packet.handshake.PacketHandshake;
import net.miaomoe.blessing.protocol.registry.State;
import net.miaomoe.blessing.protocol.version.Version;
import org.jetbrains.annotations.NotNull;

class Example {
    public final ProtocolMappings mappings = State.HANDSHAKE.serverbound.getValue();

    public @NotNull PacketHandshake getHandshake() {
        // 当不确定目标版本时 (例如在HANDSHAKE阶段) 
        // 可以使用Version.UNDEFINED来获取数据包.

        // 握手数据包的id为0 也就是0x00
        return (PacketHandshake) mappings
                .getMappings(Version.UNDEFINED, 0x00)
                .getInit()
                .get();
    }

}
```

---

## 或.. 通过数据包的Class来获取数据包本身:

> ⚠️ 尽管您可以这么做 但仍然要确保目标数据包确实注册在了对应的状态和映射中.

```java
import net.kyori.adventure.text.Component;
import net.miaomoe.blessing.nbt.chat.MixedComponent;
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings;
import net.miaomoe.blessing.protocol.packet.common.PacketDisconnect;
import net.miaomoe.blessing.protocol.registry.State;
import net.miaomoe.blessing.protocol.version.Version;
import org.jetbrains.annotations.NotNull;

class Example {
    public final ProtocolMappings mappings = State.LOGIN.clientbound.getValue();

    // Disconnect数据包不支持UNDEFINED, 使用1.20.3/4举例
    // 因为调用该数据包的时候应该已经知道目标客户端版本
    public PacketDisconnect getDisconnectPacket(final @NotNull Component reason) {
        final PacketDisconnect packet = (PacketDisconnect) mappings
                .getMappings(Version.V1_20_3, PacketDisconnect.class)
                .getInit()
                .get();
        packet.setMessage(new MixedComponent(reason));
        return packet;
    }

}
```

---

# 获得数据包ID

您可以通过数据包获取数据包ID 以在编码数据包时写入对应的id.

> ⚠️ 同样的 如果未找到对应的id 也会抛出NullPointerException异常

```java
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings;
import net.miaomoe.blessing.protocol.packet.type.PacketInterface;
import net.miaomoe.blessing.protocol.version.Version;
import org.jetbrains.annotations.NotNull;

class Utility {
    public static int getPacketId(
            final @NotNull ProtocolMappings mappings,
            final @NotNull Version version,
            final @NotNull PacketInterface packet
    ) {
        return mappings.getId(version, packet.getClass());
    }
}
```
---

# 创建数据包

您可以创造属于您自己的数据包
有关注册数据包的方法 请看下一篇

您可以按照需要实现以下接口:
  - PacketToClient : 针对于`clientbound`, 仅编码 不解码
  - PacketToServer : 针对于`serverbound`, 仅解码 不编码
  - PacketBidirectional: 当服务端和客户端都可以发送和接受此数据包时实现. 
可以编码也可以解码. 是PacketToClient和PacketToServer的结合.

> 🔧 推荐数据包拥有一个无形参构建参数.

```java
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional;
import net.miaomoe.blessing.protocol.packet.type.PacketToClient;
import net.miaomoe.blessing.protocol.util.ByteMessage;
import net.miaomoe.blessing.protocol.version.Version;
import org.jetbrains.annotations.NotNull;

class CustomPacket implements PacketBidirectional {

    @Override
    public void encode(
            @NotNull ByteMessage byteBuf,
            @NotNull Version version
    ) {
        // 什么都不做? 如果该数据包不含有数据 那么确实可以这么做.
    }

    @Override
    public void decode(
            @NotNull ByteMessage byteBuf, 
            @NotNull Version version
    ) {
        // 依旧是什么都不做
    }
}
```

# 注册数据包

如果您实现了一个数据包且想要应用它 那么注册无疑是重要的一步.

选择好状态和映射之后, 即可注册您的数据包:

```java
import net.miaomoe.blessing.protocol.mappings.PacketMapping;
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings;
import net.miaomoe.blessing.protocol.registry.State;
import net.miaomoe.blessing.protocol.version.Version;

class RegisterCustomPacket() {
    public void register() {
        final State state = State.PLAY;
        final PacketMapping packetMapping =
                PacketMapping.generate(CustomPacket::new, PacketMapping.builder()
                        .addMapping(0x01, Version.V1_7_6)
                        .addMapping(0x02, Version.V1_8, Version.V1_9)
                        .getMapping()
                );
        state.clientbound.getValue().register(packetMapping);
        state.serverbound.getValue().register(packetMapping);
    }
}
```

在该示例中, 如果使用了register()方法 
那么就会向`clientbound`和`serverbound`注册`CustomPacket`这个数据包.  
它将对1.7.6协议对应的版本注册`0x01`这个数据包id, 并对1.8-1.9版本注册`0x02`这个数据包id.
在注册之后 您就能根据之前的示例获取数据包id 根据id或class获取数据包本身了

> 🔧 除了使用PacketMapping.builder().getMapping()以外, 您也可以使用以下方法:
> - PacketMapping.withAll(packetId): 对所有版本注册数据包id
> - PacketMapping.withSingle(versionRange, packetId): 对指定的一个范围的客户端版本注册数据包id
> - PacketMapping.withSingle(fromVersion, toVersion, packetId): 与第二个方法相同 但是会自动完成VersionRange的创建
