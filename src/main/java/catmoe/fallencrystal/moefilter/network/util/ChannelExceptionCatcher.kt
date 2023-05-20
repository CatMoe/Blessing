package catmoe.fallencrystal.moefilter.network.util

import io.netty.channel.Channel
import java.io.IOException

object ChannelExceptionCatcher {
    fun handle(channel: Channel, cause: Throwable?) {
        channel.close()
        // cps ++
        if (cause is IOException) return

        // add to blacklist
    }
}
