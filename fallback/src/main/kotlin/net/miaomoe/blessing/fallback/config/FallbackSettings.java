/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.miaomoe.blessing.fallback.config;

import io.netty.channel.Channel;
import net.miaomoe.blessing.fallback.cache.PacketCacheGroup;
import net.miaomoe.blessing.fallback.cache.PacketsToCache;
import net.miaomoe.blessing.fallback.handler.FallbackHandler;
import net.miaomoe.blessing.fallback.handler.FallbackInitializer;
import net.miaomoe.blessing.fallback.handler.exception.ExceptionHandler;
import net.miaomoe.blessing.fallback.handler.motd.DefaultMotdHandlerKt;
import net.miaomoe.blessing.fallback.handler.motd.FallbackMotdHandler;
import net.miaomoe.blessing.nbt.dimension.World;
import net.miaomoe.blessing.protocol.util.PlayerPosition;
import net.miaomoe.blessing.protocol.util.Position;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;


/**
 * Settings for fallback.
 * @author FallenCrystal
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class FallbackSettings {

    private FallbackSettings() {
    }

    /**
     * Create a new settings object (Cannot access directly with constructor)
     *
     * @return FallbackSettings
     */
    public static FallbackSettings create() {
        return new FallbackSettings();
    }

    /**
     * Create a FallbackInitializer from this settings
     * @return A new FallbackInitializer but holding this settings
     */
    public @NotNull FallbackInitializer buildInitializer() {
        return new FallbackInitializer(this);
    }

    /**
     * What world should be used for send dimension?
     */
    private @NotNull World world = World.OVERWORLD;

    /**
     * What brand should be provided to the client?
     * It can be view in f3 in the client.
     * MiniMessage is supported. But only can use legacy color.
     */
    private String brand = "Blessing";

    /**
     * What should be the name of the player joining the fallback?
     */
    private String playerName = "Blessing";

    /**
     * Check whether each packet is sent in order (When not playing)
     * This is very useful to prevent exploit and debug unexpected exception.
     * If accidentally get disconnected by verifying.
     * Can trying disable this check and try again.
     */
    private boolean validate = true;

    /**
     * Disconnect when the connection is idle for more than a few milliseconds.
     */
    private long timeout = 30000;

    /**
     * What handler should be used when you need to send a motd to get what you want to return?
     */
    private @NotNull FallbackMotdHandler motdHandler = DefaultMotdHandlerKt.getDefaultFallbackMotdHandler();

    /**
     * What handler should we have to handle when an exception occurs?
     */
    private @Nullable ExceptionHandler exceptionHandler;

    /**
     * Call when FallbackInitializer#initChannel(Channel) triggered.
     */
    private @Nullable BiConsumer<FallbackHandler, Channel> initListener;

    /**
     * Logger for print debug message.
     * Can set to null to disable debug.
     */
    private @Nullable Logger debugLogger;

    /**
     * SpawnPosition to send to client.
     * Although it is very likely that the client does not need this position.
     */
    private @NotNull Position spawnPosition = new Position(7.5, 100, 7.5);

    /**
     * Position, Yaw and Pitch when joining teleporting.
     */
    private @NotNull PlayerPosition joinPosition = new PlayerPosition(spawnPosition, 180f, 0f, false);

    /**
     * What teleportId should be use for teleport?
     */
    private int teleportId = 7890;

    /**
     * Use cache for common output packet?
     */
    @ApiStatus.Experimental
    private boolean useCache = true;

    /**
     * Should we cache packets?
     * @param useCache use cache or create packet when writing (default is true)
     * @return FallbackSettings
     * @deprecated Unless used for debugging purposes. Otherwise, not using caching is not supported.
     */
    @Deprecated
    public FallbackSettings setUseCache(final boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    /**
     * A map of save some PacketCacheGroup.
     */
    private final @NotNull Map<PacketsToCache, PacketCacheGroup> cacheMap = new EnumMap<>(PacketsToCache.class);

    public final @NotNull Map<PacketsToCache, PacketCacheGroup> getCacheMap() {
        return cacheMap;
    }

    public final @NotNull World getWorld() {
        return this.world;
    }

    public final String getBrand() {
        return this.brand;
    }

    public final String getPlayerName() {
        return this.playerName;
    }

    public final boolean isValidate() {
        return this.validate;
    }

    public final long getTimeout() {
        return this.timeout;
    }

    public final @NotNull FallbackMotdHandler getMotdHandler() {
        return this.motdHandler;
    }

    public final @Nullable ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public final @Nullable BiConsumer<FallbackHandler, Channel> getInitListener() {
        return this.initListener;
    }

    public final @Nullable Logger getDebugLogger() {
        return this.debugLogger;
    }

    public final @NotNull Position getSpawnPosition() {
        return this.spawnPosition;
    }

    public final @NotNull PlayerPosition getJoinPosition() {
        return this.joinPosition;
    }

    public final int getTeleportId() {
        return this.teleportId;
    }

    public final boolean isUseCache() {
        return this.useCache;
    }

    /**
     * Settings for World (default=World.OVERWORLD)
     * @param world What world should be used for send dimension?
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setWorld(@NotNull World world) {
        this.world = world;
        return this;
    }

    /**
     * Settings for brand (default=Blessing)
     * It can be view in f3 in the client.
     * MiniMessage is supported.
     * But only can use legacy color.
     * @param brand What brand should be provided to the client?
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    /**
     * Settings for playerName (default=Blessing)
     * @param name What should be the name of the player joining the fallback?
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setPlayerName(String name) {
        this.playerName = name;
        return this;
    }

    /**
     * Check whether each packet is sent in order (When not playing)
     * This is very useful to prevent exploit and debug unexpected exception.
     * If accidentally get disconnected by verifying.
     * Can trying disable this check and try again.
     * @param validate enable or disable validate (default=true)
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setValidate(boolean validate) {
        this.validate = validate;
        return this;
    }

    /**
     * Settings for timeout (default=30000L)
     * @param timeout Disconnect when the connection is idle for more than a few milliseconds.
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * What handler should be used when you need to send a motd to get what you want to return?
     * @param motdHandler custom FallbackMotdHandler
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setMotdHandler(@NotNull FallbackMotdHandler motdHandler) {
        this.motdHandler = motdHandler;
        return this;
    }

    /**
     * What handler should we have to handle when an exception occurs?
     * @param exceptionHandler custom handler. If set to null. Fallback server will disconnect client.
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setExceptionHandler(@Nullable ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Call when FallbackInitializer#initChannel(Channel) triggered.
     * @param initListener custom listener
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setInitListener(@Nullable BiConsumer<FallbackHandler, Channel> initListener) {
        this.initListener = initListener;
        return this;
    }

    /**
     * Logger for print debug message.
     * @param debugLogger logger to debug. set null to disabling debug feature.
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setDebugLogger(@Nullable Logger debugLogger) {
        this.debugLogger = debugLogger;
        return this;
    }

    /**
     * SpawnPosition for client.
     * Although it is very likely that the client does not need this position.
     * @param spawnPosition SpawnPosition to send to client (default=Position(x=7.5 y=100 z=7.5))
     * @return FallbackSettings for chain setters.
     */
    public final FallbackSettings setSpawnPosition(@NotNull Position spawnPosition) {
        this.spawnPosition = spawnPosition;
        return this;
    }

    /**
     * Position, Yaw and Pitch when joining teleporting.
     * Default Position: x 7.5, y 100, z 7.5, yaw 180, pitch 0
     * @param joinPosition position sent to client when joining.
     * @return FallbackSettings for chain setters
     */
    public final FallbackSettings setJoinPosition(@NotNull PlayerPosition joinPosition) {
        this.joinPosition = joinPosition;
        return this;
    }

    /**
     * What teleportId should be use for teleporting? (1.9+ clients)
     * @param teleportId for sent to client when teleporting (with joinPosition)
     * @return FallbackSettings for chain setters
     */
    public final FallbackSettings setTeleportId(int teleportId) {
        this.teleportId = teleportId;
        return this;
    }
}
