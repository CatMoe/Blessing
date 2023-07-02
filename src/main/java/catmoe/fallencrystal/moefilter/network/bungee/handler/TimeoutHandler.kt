package catmoe.fallencrystal.moefilter.network.bungee.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

class TimeoutHandler @JvmOverloads constructor(timeout: Long, timeUnit: TimeUnit? = TimeUnit.MILLISECONDS) : IdleStateHandler(timeout, 0L, 0L, timeUnit) {
    private var closed = false

    @Deprecated("")
    constructor() : this(12L, TimeUnit.SECONDS)

    @Throws(Exception::class)
    override fun channelIdle(ctx: ChannelHandlerContext, idleStateEvent: IdleStateEvent) { assert(idleStateEvent.state() == IdleState.READER_IDLE); readTimedOut(ctx) }

    @Throws(Exception::class)
    private fun readTimedOut(ctx: ChannelHandlerContext) { if (!closed) { if (ctx.channel().isActive) { ctx.close() }; closed = true } }
}