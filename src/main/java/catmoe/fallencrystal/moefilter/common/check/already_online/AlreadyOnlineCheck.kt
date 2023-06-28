package catmoe.fallencrystal.moefilter.common.check.already_online

import catmoe.fallencrystal.moefilter.common.check.AbstractCheck
import catmoe.fallencrystal.moefilter.common.check.info.CheckInfo
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import net.md_5.bungee.BungeeCord

class AlreadyOnlineCheck : AbstractCheck() {
    override fun increase(info: CheckInfo): Boolean { return BungeeCord.getInstance().getPlayer((info as Joining).username) == null }
}