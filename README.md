<!--- @formatter:off --->
<div align="center">

![ba-icon.png](https://i.postimg.cc/fLTbc7VB/ba-icon.png)
![Code-Size](https://img.shields.io/github/languages/code-size/CatMoe/MoeFilter?style=flat-square)
![Release](https://img.shields.io/github/v/release/FloraCore/FloraCore?style=flat-square)
![License](https://img.shields.io/github/license/CatMoe/MoeFilter?style=plastic)
![Actions](https://img.shields.io/github/actions/workflow/status/CatMoe/MoeFilter/test.yml?style=flat-square)

</div>

# Moe Filter  
  
⚡ Powerful high-performance BungeeCord utils. Including built-in antibot features.  (Minecraft 1.7.6-1.20.2)

  
#### Looking for readme in other languages?

- [简体中文](https://github.com/CatMoe/MoeFilter/blob/stray/readme/CN.md)
- [English](https://github.com/CatMoe/MoeFilter/blob/stray/readme/EN.md)

---

## ✨ Features:
### Antibot:  
- Players connected via geyser will be bypassed
> Only working with you is using Geyser-BungeeCord and Floodgate!
  - Configurable Ping & Join mixed checks
  - Disconnect with pre-made packets to preserve performance
  - Do not call `PreLoginEvent` for clients when they are already disconnected
  - Clean console. Don't even need a log filter. `Only when using limbo.`
  - You can customize how all inspection modules work
  - Virtual server to prevent bot / attack bypass / overload proxy.

   Checks that have been implemented:
   - Ping & Join Combined checks.
   - Username checking (to avoid invalid or bots username)
   - Connection domain via check
   - Geolocation (GeoIP) check
   - Check if the player is already online (Avoid forcing to kick online player for same username)
   - Similar Name Check (Beta)
   - Prevent bot/player using proxy (built-in proxy scanner + third-party api)
   - Falling check in virtual server.

> The final effect depends on the working mode you choose in `antibot.conf`

### General:
- Cache message packets
    - Avoid huge overhead when sending repeatedly or to multiple players
- Full MiniMessage supports
    - Also, you can use hex / gradient colors anywhere
    - (Not available for player client versions below 1.16.x and some BungeeCord logger.)
- Design with synchrony in mind
    - Well, we're not actually all async because in some cases it is simply not worth using async.
    - BungeeCord will never lag because of it.
- Consider it from a compatibility and performance perspective
    - Is there anything MoeFilter isn't compatible with?
    - If you really encounter such problems. You should go and open an [issue](https://github.com/CatMoe/MoeFilter/issues)
- Easy-to-use interface. Your next BungeeCord utility plugin.
    - Hey! Stop buying bells and whistles of BungeeCord forks.  
      It's not worth spending your money on those.

---

## 🔧 Supports BungeeCord forks / Plugins:

> **Velocity support will be added later (no ETA). Spigot is not supported at all**

| BungeeCord & forks | Support |
|--------------------|---------|
| BungeeCord         | ✅       |
| Waterfall          | ✅       |
| Flame/MongoCord    | ✅       |
| BotFilter          | ✅       |
| BetterBungee       | 🛑      |
| Travertine         | ❔       |
| XCord              | ❔       |
| NullCordX          | ☑️      |

| Plugins         | Support |
|-----------------|---------|
| nAntiBot        | 🛑      |
| HAProxyDetector | ❔       |
| JH AntiBot      | 🛑      |
| Protocolize     | ✅       |
| JPremium        | ☑️      |
| ViaVersion      | 🛑      |
| Triton          | ☑️      |

Useful links:
  - [How to make JPremium compatible with MoeFilter](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714907598)
  - [Do not install ViaVersion / ProtocolSupport and its extension dependencies on the proxy](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714924303)

> ✅: Full supported. Can use all MoeFilter's features with it

> ☑️: Is supported. But some features may not work as expected

> ❔: Not tested. But it should work.

> 🛑: Not supported. We will not be responsible for any unexpected actions resulting from the use of this BungeeCord / Plugin

---

## 📷 Previews

> There's nothing here yet.

If you want to publish your own preview. Please dm me `FallenCrystal`

---

## 📚 Credits

This project contains content borrowed from the following website/repository.
- [BungeeCord](https://github.com/SpigotMC/BungeeCord)
- [Velocity](https://github.com/PaperMC/Velocity) `References such as decoders`
- [Sonar](https://github.com/jonesdevelopment/sonar) `Code that reflects SERVER_CHILD aspects of robbery`
- [EpicGuard](https://github.com/awumii/EpicGuard) `GeoIP initialization and checking`
- [MaxMind](https://maxmind.com/) `GeoIP database.`
- [NanoLimbo](https://github.com/Nan1t/NanoLimbo) `Basic limbo server`
- [BotFilter](https://github.com/Leymooo/BungeeCord) `Dimension, Chunk packets`
- [wiki.vg](https://wiki.vg/Protocol) `1.7.6-Latest Minecraft version protocol`
- [NullCordX](https://builtbybit.com/resources/nullcordx-lightweight-antibot.22322/) `proxy list providers.`
- [FlameCord](https://builtbybit.com/resources/flamecord-the-ultimate-antibot.13492/) `proxy list providers.`
- [PacketLimiter](https://github.com/Spottedleaf/PacketLimiter) `Refer to Packet Limiter.`

Thanks for all the above items. To help us structure these things better.

---

#### Contributors:

![Contributors](https://contrib.rocks/image?repo=CatMoe/MoeFilter)

![Alt](https://repobeats.axiom.co/api/embed/f665cd4fc79f5d2012357d69f3ea2a1f505d77cf.svg "Repeats analytics image")

Made by [@CatMoe](https://github.com/CatMoe) / [@FallenCrystal](https://github.com/FallenCrystal) on 🌏 with 💖
