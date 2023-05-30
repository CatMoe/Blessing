package catmoe.fallencrystal.moefilter.util.plugin.luckperms

import java.util.*
import kotlin.concurrent.schedule

class LuckPermsRegister {
    private fun isAvailable(): Boolean { return try { net.luckperms.api.LuckPermsProvider.get(); true }
    catch (e: NoClassDefFoundError) { false } }

    fun register() { if (isAvailable()) { LuckPermsListener.registerEvent() } else { Timer().schedule(1000) { register() } } }
}