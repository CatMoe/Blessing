package catmoe.fallencrystal.moefilter.common.blacklist

import java.util.*

/*
  Copy from AkaneField - Author: FallenCrystal
  https://github.com/CatMoe/AkaneField/blob/main/src/main/java/catmoe/fallencrystal/akanefield/common/objects/profile/BlackListProfile.kt
 */
class BlacklistProfile {
    val id: String
    val name: String?
    val reason: String
    val ip: String

    constructor(ip: String, reason: String) {
        this.ip = ip
        id = UUID.randomUUID().toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        name = ""
        this.reason = reason
    }

    constructor(ip: String, reason: String, id: String, name: String?) {
        this.id = id
        this.reason = reason
        this.name = name
        this.ip = ip
    }

    constructor(ip: String, reason: String, name: String?) {
        id = UUID.randomUUID().toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        this.reason = reason
        this.name = name
        this.ip = ip
    }

    val isNamePresent: Boolean
        get() = name != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlacklistProfile) return false
        return id == other.id && ip == other.ip
    }

    override fun hashCode(): Int {
        return Objects.hash(id, ip)
    }
}