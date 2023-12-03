<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
  <img src="https://raw.githubusercontent.com/CatMoe/MoeFilter/stray/readme/icon/ba-icon.png" alt="ba-icon">

***⚡ A open-source antibot & utils for BungeeCord.***

[![Release](https://img.shields.io/github/v/release/CatMoe/MoeFilter?style=flat-square)](https://github.com/CatMoe/MoeFilter/releases/latest)
[![Download](https://img.shields.io/github/downloads/CatMoe/MoeFilter/total?style=flat-square)](https://github.com/CatMoe/MoeFilter/releases/latest)
[![Stars](https://img.shields.io/github/stars/CatMoe/MoeFilter?style=flat-square)](https://github.com/CatMoe/MoeFilter/stargazers)
[![Issues](https://img.shields.io/github/issues/CatMoe/MoeFilter?style=flat-square)](https://github.com/CatMoe/MoeFilter/issues)
[![License](https://img.shields.io/github/license/CatMoe/MoeFilter?style=flat-square)](https://github.com/CatMoe/MoeFilter/blob/0.1.4-Hotfix1/LICENSE)
</div>
<div align="center">

[简体中文](https://github.com/CatMoe/MoeFilter/blob/stray/readme/CN.md) |
[English](https://github.com/CatMoe/MoeFilter/blob/stray/readme/EN.md)
</div>


## About

MoeFilter is an antibot and utility plugin developed specifically for BungeeCord.
Can run on almost any fork you like.

## Features ✨

> Antibot
  - Players connected via Geyser and Floodgate are not checked.
  - Don't need to modify the proxy bytecode file.
  - Third-party cloud services are not required.
  - No need to call the `PreLoginEvent` on the BungeeCord.
  - Each antibot module is almost customizable.
  - Don't lock your server when under attacks.
  - Stand-alone virtual server platform for filtering attacks.
  - Packet order check. Prevent spam attacks from non-Minecraft protocols.
  - Basic antibot checks like `valid username`, `reconnect`, `GeoIP`, and more...
> Translation
  - Register commands with ease. async execute, aliases, player-only operations, etc. can be done with annotations.
  - [`PlayerPostBrandEvent`](https://github.com/CatMoe/MoeFilter/blob/stray/translation/src/main/kotlin/catmoe/fallencrystal/translation/event/events/player/PlayerPostBrandEvent.kt). 
    Call a specific listener when the player sends their own client brand.
  - Easily write code that works on both [Velocity](https://github.com/PaperMC/Velocity) and [BungeeCord](https://github.com/SpigotMC/BungeeCord). 
    Simply install MoeFilter to your server.
  - All are designed with [MiniMessage](https://docs.advntr.dev/minimessage/) in mind.


## Compatibility 🔧

> ⚠️ **The Velocity version is only for driving MoeTranslation.**
> Antibot compatibility with other platforms will not be considered.
> [Learn more](https://github.com/CatMoe/MoeFilter/tree/stray/readme/postscript)

| BungeeCord & forks | Support |
|--------------------|---------|
| BungeeCord         | ✅       |
| Waterfall          | ✅       |
| FlameCord          | ✅       |
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
| ViaVersion *    | 🛑      |
| Triton          | ☑️      |

> ✅: Full supported.
> Can use all MoeFilter's features with it  
> ☑️: Is supported.
> But some features may not work as expected.
> ❔: Not tested.
> But it should work.  
> 🛑: Not supported. 
> We will not be responsible for any unexpected actions resulting from the use of this proxy fork / plugin.

> If you have a question about the compatibility of one of the products listed above with MoeFilter. 
> Please open an issue [here](https://github.com/CatMoe/MoeFilter/issues).
> Or try to find answer in [here](https://github.com/CatMoe/MoeFilter/issues/56).

> *: To use ViaVersion. Install it on the backend server instead of on the proxy. 
> [Learn More](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714924303)

## 📷 Previews

> There's nothing here yet.

## Credits 📚

- [BungeeCord](https://github.com/SpigotMC/BungeeCord)
- [Velocity](https://github.com/PaperMC/Velocity)
- [Sonar](https://github.com/jonesdevelopment/sonar)
- [EpicGuard](https://github.com/awumii/EpicGuard)
- [MaxMind](https://maxmind.com/)
- [NanoLimbo](https://github.com/Nan1t/NanoLimbo)
- [BotFilter](https://github.com/Leymooo/BungeeCord)
- [wiki.vg](https://wiki.vg/Protocol)
- [NullCordX](https://builtbybit.com/resources/nullcordx-lightweight-antibot.22322/)
- [FlameCord](https://builtbybit.com/resources/flamecord-the-ultimate-antibot.13492/)
- [PacketLimiter](https://github.com/Spottedleaf/PacketLimiter)

## Contributors

[
![Contributors](https://contrib.rocks/image?repo=CatMoe/MoeFilter)
![Alt](https://repobeats.axiom.co/api/embed/f665cd4fc79f5d2012357d69f3ea2a1f505d77cf.svg)
](https://github.com/CatMoe/MoeFilter/graphs/contributors)

Made by [@CatMoe](https://github.com/CatMoe) / [MoeFilter Contributors](https://github.com/CatMoe/MoeFilter/graphs/contributors) on 🌏 with 💖

