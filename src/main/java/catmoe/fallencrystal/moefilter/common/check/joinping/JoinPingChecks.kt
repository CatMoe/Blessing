package catmoe.fallencrystal.moefilter.common.check.joinping

import catmoe.fallencrystal.moefilter.common.check.joinping.JoinPingType.*
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.util.message.kick.KickType
import com.github.benmanes.caffeine.cache.Caffeine

object JoinPingChecks {

    // Address, Name (  )

    private val joinCache = Caffeine.newBuilder().build<String, String>()
    private val pingCache = Caffeine.newBuilder().build<String, Boolean>()

    private var autoMethod = JOIN_FIRST_REST

    private val config = ObjectConfig.getConfig()

    private val isAutoEnabled = try { config.getBoolean("ping-and-join.AUTO") } catch (ex: Exception) { true }
    private val idleMethod = if (isAutoEnabled) { autoMethod } else { try { (config.getAnyRef("ping-and-join.IDLE") as JoinPingType) } catch (ex: Exception) { JOIN_FIRST_REST } }
    private val attackMethod = if (isAutoEnabled) { autoMethod } else { try { (config.getAnyRef("ping-and-join.DURING-ATTACK") as JoinPingType) } catch (ex: Exception) { PING_FIRST_REST } }

    private var isInAttack = false

    fun onPlayerJoin(address: String, name: String): KickType? { return if (isInAttack) checkJoin(address, name, attackMethod) else checkJoin(address, name, idleMethod) }

    private fun checkJoin(address: String, name: String, type: JoinPingType): KickType? {
        val isJoined = joinCache.getIfPresent(address)
        when (type) {
            DISABLED -> { return null }
            STABLE -> { return if (isJoined == null) { joinCache.put(address, name); KickType.REJOIN } else if (!isPinged(address)) { KickType.PING_FIRST } else null }
            ONLY_JOIN -> { return if (isJoined == null) { joinCache.put(address, name); KickType.REJOIN } else null }
            ONLY_PING -> { if (isPinged(address)) { return null } else KickType.PING_FIRST }
            PING_FIRST -> { return if (!isPinged(address)) { KickType.PING_FIRST } else if (isJoined == null) { joinCache.put(address, name); KickType.REJOIN } else null }
            JOIN_FIRST -> { return if (isJoined == null) { joinCache.put(address, name); KickType.PING_FIRST } else if (isPinged(address)) { null } else KickType.PING_FIRST }
            PING_FIRST_REST -> {
                return if (!isPinged(address)) { KickType.PING_FIRST } else if (isJoined == null) { joinCache.put(address, name); KickType.REJOIN }
            else if (checkRest(address, name)) { null } else { KickType.REJOIN } } // 他们实际上已经被黑名单了 在下次加入时JoinListener处会以黑名单的理由踢出并告诉他们理由.
            JOIN_FIRST_REST -> { return if (isJoined == null) { joinCache.put(address, name); KickType.PING_FIRST } else if (!isPinged(address)) { KickType.PING_FIRST } else if (checkRest(address, name)) { null } else { KickType.REJOIN } }
        }
        return null
    }

    fun onPing(address: String) { pingCache.put(address, true) }

    private fun checkPing(address: String, method: JoinPingType) {
        when (method) {
            DISABLED -> {}
            STABLE -> pingCache.put(address, true)
            ONLY_JOIN -> {}
            ONLY_PING -> pingCache.put(address, true)
            PING_FIRST -> pingCache.put(address, true)
            JOIN_FIRST -> if (joinCache.getIfPresent(address) != null) pingCache.put(address, true)
            PING_FIRST_REST -> pingCache.put(address, true)
            JOIN_FIRST_REST -> if (joinCache.getIfPresent(address) != null) pingCache.put(address, true)
        }
    }

    private fun isPinged(address: String): Boolean { return pingCache.getIfPresent(address) ?: false }

    fun setAutoMethod(method: JoinPingType) { autoMethod = method }

    fun setInAttack(yes: Boolean) { isInAttack = yes }

    private fun checkRest(address: String, name: String): Boolean {
        val checkName = joinCache.getIfPresent(address) ?: name
        // return if (checkName != name) { // Blacklist false } else true
        return checkName == name
    }
}