## About

MoeFilter is an antibot and utility plugin developed specifically for BungeeCord.
Can run on almost any fork you like.
(Such as [Waterfall](https://github.com/PaperMC/Waterfall/), etc.)

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
- Cache motd packets to significantly reduce the overhead on large networks.
- Basic antibot checks like `valid username`, `reconnect`, `GeoIP`, and more...
> Translation
- Register commands with ease. async execute, aliases, player-only operations, etc. can be done with annotations.
- [`PlayerPostBrandEvent`](https://github.com/CatMoe/MoeFilter/blob/stray/translation/src/main/kotlin/catmoe/fallencrystal/translation/event/events/player/PlayerPostBrandEvent.kt).
  Call a specific listener when the player sends their own client brand.
- Use MoeTranslation to easily write code that is compatible with both [Velocity](https://github.com/PaperMC/Velocity) and [BungeeCord](https://github.com/SpigotMC/BungeeCord).
- All the features about chat are made with [MiniMessage](https://docs.advntr.dev/minimessage/) in mind.


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
| JPremium        | ✅       |
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

> *: To use ViaVersion. 
> Install it on the backend server instead of on the proxy.
> [Learn More](https://github.com/CatMoe/MoeFilter/issues/56#issuecomment-1714924303)

## 📷 Previews

> There's nothing here yet.