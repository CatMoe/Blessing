package catmoe.fallencrystal.moefilter.network.handler;

import catmoe.fallencrystal.moefilter.network.pipeline.IPipeline;
import catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ConnectionThrottle;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LoginRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class PlayerHandler extends InitialHandler implements IPipeline {
    public PlayerHandler(final ChannelHandlerContext ctx, final ListenerInfo listener, final ConnectionThrottle throttler) {
        super(BungeeCord.getInstance(), listener);
        this.ctx = ctx;
        this.throttler = throttler;
    }

    private final ConnectionThrottle throttler;
    public InetAddress inetAddress;
    public final ChannelHandlerContext ctx;
    public ChannelPipeline pipeline;


    @Override
    public void connected(final ChannelWrapper wrapper) throws Exception { super.connected(wrapper); pipeline = wrapper.getHandle().pipeline(); inetAddress = ((InetSocketAddress) getSocketAddress()).getAddress(); }

    @Override
    public void exception(final Throwable t) throws Exception {ChannelExceptionCatcher.INSTANCE.handle(ctx.channel(), t);}

    @Override
    public void handle(final PacketWrapper packet) throws Exception {
        if (packet == null) { return; }
        // TODO PacketLimit here if (packet.buf.readableBytes() > ?(Int) {packet.buf.clear(); }
    }

    @Override
    public void handle(final EncryptionResponse response) throws Exception { /* ENCRYPTION PER SEC */ super.handle(response); }

    @Override
    public void handle(final Handshake handshake) throws Exception {
        switch (handshake.getRequestedProtocol()) {
            case 1: { /* Ping */ }
            case 2: { /* Join */ }
            default: { /* Invalid Protocol */ }
        }

        // Blacklist check here
    }

    private static final Map<String, Kick> kickPacketCache = new HashMap<>();
    public void disconnect_(final String reason) {
        if (reason != null && ctx.channel().isActive()) {
            cache: {
                Kick packet = kickPacketCache.get(reason);
                if (packet != null) {ctx.channel().writeAndFlush(packet);break cache;}
                packet = new Kick(ComponentSerializer.toString(new TextComponent(reason)));
                kickPacketCache.put(reason, packet);
                ctx.channel().writeAndFlush(packet);
            }
        }
        ctx.close();
    }

    @Override
    public void handle(final LoginRequest loginRequest) throws Exception {
        if (throttler != null && throttler.throttle(getSocketAddress())) { ctx.close(); return; }
        // TODO Verify here.
    }

    @Override
    public String toString() { return "§7(§f" + getSocketAddress() + (getName() != null ? "|" + getName() : "") + "§7) <-> MoeFilter InitialHandler"; }
}
