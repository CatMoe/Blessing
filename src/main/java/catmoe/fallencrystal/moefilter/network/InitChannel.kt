package catmoe.fallencrystal.moefilter.network

import catmoe.fallencrystal.moefilter.network.bungee.pipeline.BungeePipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.ReflectionUtils
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.ProxyServer

class InitChannel {
    private val knownIncompatibilitiesBungee = listOf("NullCordX", "XCord")
    private val knownIncompatibilitiesPlugin = listOf("AntiAttackRL", "HAProxyDetector")

    private val bungee = ProxyServer.getInstance()

    fun initPipeline() {
        log("Starting inject MoeFilter PipelineUtils...")
        val proxyName = bungee.name
        for (it in knownIncompatibilitiesBungee) { if (it.contains(proxyName)) { log("&cFailed to inject because incompatibilities for $it bungeecord fork!"); bungee.stop(); return } }
        for (it in knownIncompatibilitiesPlugin) { if (bungee.pluginManager.getPlugin(it) != null) { log("&cFailed to inject because the plugin $it is competing for the pipeline. Please unload that plugin first."); bungee.stop(); return } }
        if (!ReflectionUtils().inject(BungeePipeline()).get()) { log("&cFailed to inject pipeline. Please report this issue for CatMoe!") } else { log("&aPipeline inject successfully.") }
    }

    private fun log(text: String) {
        MessageUtil.logWarn("[MoeFilter] [Pipeline] $text")
    }
}