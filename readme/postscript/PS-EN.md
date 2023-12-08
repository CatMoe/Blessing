# P.S. about this item or content not in the readme

This is what should have been in the readme. But for part of the reason it is not written in the readme.  
If you're curious, or you're wondering about some content in the readme, maybe these will answer these questions.

## About the content in the original #General being removed:

That paragraph contains some controversial or misleading content. It was eventually removed under consideration.  
After all, the user can barely perceive what these things are bringing to them

> Off topic: BungeeCord itself kicks out cache packets are no longer available. Because in
> [0f5f09b](https://github.com/SpigotMC/BungeeCord/commit/0f5f09b6c5f073130515c8cd435541c5c68bcba8)
> commit. Each time a packet is written, the BaseComponent will be converted to Json or NBT and then written.
> I'm also not interested in writing directly to ByteBuf or anything like that. 
> If I want to do that I may also need to change my design.
> TL;DR: The update to BungeeCord broke it (cached packet kick). I don't want to fix it. Use a virtual server.

---

About "Don't spend your money on other BungeeCord forks". 
It's a bit against my original intention in the literal sense.  
Because there are so many shoddy forks, I'm essentially trying to get people to avoid buying or using those forks. 
(Such as Aegis, GuardHexa.)  
They often introduce changes that break plugin compatibility
There may even be plugin issues caused by forks, which can cause unnecessary headaches for plugin developers.
Even the plugin works fine on an unmodified BungeeCord.

About high performance,
MoeFilter's "high performance" is reflected in the anti-bot aspect. And not BungeeCord itself.  
It is also unlikely to have the ability to improve the performance of the BungeeCord itself.

And considering that it was created as a plugin so that it can be used on your favorite BungeeCord (or fork).
I'm not against you buying those forks, 
and I wouldn't recommend you to buy them. Because a lot of times, free is the best.

> I don't guarantee that the plugin will work after the proxy has modified something in terms of protocol. 
> I'm also not obligated to be compatible with those forks.
> Considering that most of those forks already have built-in antibot functionality, 
> you'll most likely have to choose one over the other.

---

## ~~About iconic content that can't be removed~~

Because I don't want the servers to be trying to hide the antibot they are using.
> However, I can still tell what anti-bot the server is using based on the characteristics of the plugin.

The initial plan was to hide those things if you buy Premium or donate to us.  
However, since the project was open source from the beginning and had a utility purpose, 
commercialization was not suitable for this project.
Ultimately leading to such a plan being almost impossible. Probably no one cares about the iconic content either.
> I haven't made a penny on this project until now
> The project was decided at the outset that "it is almost impossible to make a profit"
>
> The right way to make money is probably to create a fork of BungeeCord and add or even copy and paste other people's code.
> And exaggerate its purpose and function. But it won't be too complicated to implement it.

---

## About Velocity version?

Scrapped. The Velocity version is only for driving MoeTranslation.  
There are already a number of excellent antibot that can work on Velocity. For example:
- [Sonar](https://github.com/jonesdevelopment/sonar)
- [nAntiBot](https://modrinth.com/plugin/nantibot)
- [LimboFilter](https://github.com/Elytrium/LimboFilter)

> To be honest, 
> I'm not familiar with Velocity's methods. 
> And I don't use Velocity very often. 
> I don't have to fix all this anymore.
> It's better to rely on BungeeCord all the time.

<!--suppress HtmlDeprecatedAttribute -->
<a href="https://github.com/FallenCrystal">
        <p align="right">@FallenCrystal</p>
</a>
<p align="right">2023-12-8</p>
