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

package net.miaomoe.blessing.protocol.registry;

import net.miaomoe.blessing.protocol.mappings.PacketMapping;
import net.miaomoe.blessing.protocol.mappings.ProtocolMappings;
import net.miaomoe.blessing.protocol.packet.common.PacketClientConfiguration;
import net.miaomoe.blessing.protocol.packet.common.PacketDisconnect;
import net.miaomoe.blessing.protocol.packet.common.PacketKeepAlive;
import net.miaomoe.blessing.protocol.packet.common.PacketPluginMessage;
import net.miaomoe.blessing.protocol.packet.configuration.PacketFinishConfiguration;
import net.miaomoe.blessing.protocol.packet.configuration.PacketRegistryData;
import net.miaomoe.blessing.protocol.packet.handshake.PacketHandshake;
import net.miaomoe.blessing.protocol.packet.login.PacketLoginAcknowledged;
import net.miaomoe.blessing.protocol.packet.login.PacketLoginPluginMessage;
import net.miaomoe.blessing.protocol.packet.login.PacketLoginRequest;
import net.miaomoe.blessing.protocol.packet.login.PacketLoginResponse;
import net.miaomoe.blessing.protocol.packet.play.*;
import net.miaomoe.blessing.protocol.packet.status.PacketStatusPing;
import net.miaomoe.blessing.protocol.packet.status.PacketStatusRequest;
import net.miaomoe.blessing.protocol.packet.status.PacketStatusResponse;
import net.miaomoe.blessing.protocol.util.LazyInit;
import net.miaomoe.blessing.protocol.version.Version;
import net.miaomoe.blessing.protocol.version.VersionRange;

import static net.miaomoe.blessing.protocol.mappings.PacketMapping.*;
import static net.miaomoe.blessing.protocol.version.Version.*;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public enum State {
    HANDSHAKE{
        {
            final ProtocolMappings serverbound = this.serverbound.getValue();
            serverbound.register(PacketHandshake::new, withAll(0x00));
        }
    },
    STATUS{
        {
            final ProtocolMappings clientbound = this.clientbound.getValue();
            final ProtocolMappings serverbound = this.serverbound.getValue();
            serverbound.register(PacketStatusRequest::new, withAll(0x00));
            clientbound.register(PacketStatusResponse::new, withAll(0x00));
            final PacketMapping ping = generate(PacketStatusPing::new, withAll(0x01));
            serverbound.register(ping);
            clientbound.register(ping);
        }
    },
    LOGIN{
        {
            final ProtocolMappings clientbound = this.clientbound.getValue();
            final ProtocolMappings serverbound = this.serverbound.getValue();
            serverbound.register(PacketLoginRequest::new, withAll(0x00));
            clientbound.register(PacketLoginResponse::new, withAll(0x02));
            serverbound.register(PacketLoginPluginMessage::new, withAll(0x02));
            clientbound.register(PacketLoginPluginMessage::new, withAll(0x04));
            clientbound.register(PacketDisconnect::new, withAll(0x00));
            serverbound.register(PacketLoginAcknowledged::new, withSingle(V1_20_2, V1_20_4, 0x03));
        }
    },
    CONFIGURATION{
        {
            final ProtocolMappings clientbound = this.clientbound.getValue();
            final ProtocolMappings serverbound = this.serverbound.getValue();
            final VersionRange range = VersionRange.of(V1_20_2, Version.Companion.getMax());
            serverbound.register(PacketClientConfiguration::new, withSingle(range, 0x00));
            clientbound.register(PacketDisconnect::new, withSingle(range, 0x01));
            serverbound.register(PacketPluginMessage::new, withSingle(range, 0x01));
            clientbound.register(PacketPluginMessage::new, withSingle(range, 0x00));
            final PacketMapping keepAlive = generate(PacketKeepAlive::new, withSingle(range, 0x03));
            serverbound.register(keepAlive);
            clientbound.register(keepAlive);
            final PacketMapping finishConfiguration = generate(PacketFinishConfiguration::new, withSingle(range, 0x02));
            serverbound.register(finishConfiguration);
            clientbound.register(finishConfiguration);
            clientbound.register(PacketRegistryData::new, withSingle(range, 0x05));
        }
    },
    PLAY{
        {
            final ProtocolMappings clientbound = this.clientbound.getValue();
            final ProtocolMappings serverbound = this.serverbound.getValue();
            clientbound.register(PacketJoinGame::new, builder()
                    .addMapping(0x01, V1_7_2, V1_8)
                    .addMapping(0x23, V1_9, V1_12_2)
                    .addMapping(0x25, V1_13, V1_14_4)
                    .addMapping(0x26, V1_15, V1_15_2)
                    .addMapping(0x25, V1_16, V1_16_1)
                    .addMapping(0x24, V1_16_2, V1_16_4)
                    .addMapping(0x26, V1_17, V1_18_2)
                    .addMapping(0x23, V1_19)
                    .addMapping(0x25, V1_19_1)
                    .addMapping(0x24, V1_19_3)
                    .addMapping(0x28, V1_19_4, V1_20)
                    .addMapping(0x29, V1_20_2, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketSpawnPosition::new, builder()
                    .addMapping(0x05, V1_7_2, V1_8)
                    .addMapping(0x43, V1_9, V1_11_2)
                    .addMapping(0x45, V1_12)
                    .addMapping(0x46, V1_12_1, V1_12_2)
                    .addMapping(0x49, V1_13, V1_13_2)
                    .addMapping(0x4D, V1_14, V1_14_4)
                    .addMapping(0x4E, V1_15, V1_15_2)
                    .addMapping(0x42, V1_16, V1_16_4)
                    .addMapping(0x4B, V1_17, V1_18_2)
                    .addMapping(0x4A, V1_19)
                    .addMapping(0x4D, V1_19_1)
                    .addMapping(0x4C, V1_19_3)
                    .addMapping(0x50, V1_19_4, V1_20)
                    .addMapping(0x52, V1_20_2)
                    .addMapping(0x54, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketDisconnect::new, builder()
                    .addMapping(0x40, V1_7_6, V1_8)
                    .addMapping(0x1A, V1_9, V1_12_2)
                    .addMapping(0x1B, V1_13, V1_13_2)
                    .addMapping(0x1A, V1_14, V1_14_4)
                    .addMapping(0x1B, V1_15, V1_15_2)
                    .addMapping(0x1A, V1_16, V1_16_1)
                    .addMapping(0x19, V1_16_2, V1_16_4)
                    .addMapping(0x1A, V1_17, V1_18_2)
                    .addMapping(0x17, V1_19)
                    .addMapping(0x19, V1_19_1)
                    .addMapping(0x17, V1_19_3)
                    .addMapping(0x1A, V1_19_4, V1_20)
                    .addMapping(0x1B, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketKeepAlive::new, builder()
                    .addMapping(0x00, V1_7_6, V1_8)
                    .addMapping(0x0B, V1_9, V1_11)
                    .addMapping(0x0C, V1_12)
                    .addMapping(0x0B, V1_12_1, V1_12_2)
                    .addMapping(0x0E, V1_13, V1_13_2)
                    .addMapping(0x0F, V1_14, V1_15_2)
                    .addMapping(0x10, V1_16, V1_16_4)
                    .addMapping(0x0F, V1_17, V1_18_2)
                    .addMapping(0x11, V1_19)
                    .addMapping(0x12, V1_19_1)
                    .addMapping(0x11, V1_19_3)
                    .addMapping(0x12, V1_19_4, V1_20)
                    .addMapping(0x14, V1_20_2)
                    .addMapping(0x15, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketKeepAlive::new, builder()
                    .addMapping(0x00, V1_7_6, V1_8)
                    .addMapping(0x1F, V1_9, V1_12_2)
                    .addMapping(0x21, V1_13, V1_13_2)
                    .addMapping(0x20, V1_14, V1_14_4)
                    .addMapping(0x21, V1_15, V1_15_2)
                    .addMapping(0x20, V1_16, V1_16_1)
                    .addMapping(0x1F, V1_16_2, V1_16_4)
                    .addMapping(0x21, V1_17, V1_18_2)
                    .addMapping(0x1E, V1_19)
                    .addMapping(0x20, V1_19_1)
                    .addMapping(0x1F, V1_19_3)
                    .addMapping(0x23, V1_19_4, V1_20)
                    .addMapping(0x24, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketPluginMessage::new, builder()
                    .addMapping(0x17, V1_7_6, V1_8)
                    .addMapping(0x09, V1_9, V1_11_1)
                    .addMapping(0x0A, V1_12)
                    .addMapping(0x09, V1_12_1, V1_12_2)
                    .addMapping(0x0A, V1_13, V1_13_2)
                    .addMapping(0x0B, V1_14, V1_16_4)
                    .addMapping(0x0A, V1_17, V1_18_2)
                    .addMapping(0x0C, V1_19)
                    .addMapping(0x0D, V1_19_1)
                    .addMapping(0x0C, V1_19_3)
                    .addMapping(0x0D, V1_19_4, V1_20)
                    .addMapping(0x0F, V1_20_2)
                    .addMapping(0x10, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketPluginMessage::new, builder()
                    .addMapping(0x3F, V1_7_6, V1_8)
                    .addMapping(0x18, V1_9, V1_12_2)
                    .addMapping(0x19, V1_13, V1_13_2)
                    .addMapping(0x18, V1_14, V1_14_4)
                    .addMapping(0x19, V1_15, V1_15_2)
                    .addMapping(0x18, V1_16, V1_16_1)
                    .addMapping(0x17, V1_16_2, V1_16_4)
                    .addMapping(0x18, V1_17, V1_18_2)
                    .addMapping(0x15, V1_19)
                    .addMapping(0x16, V1_19_1)
                    .addMapping(0x15, V1_19_3)
                    .addMapping(0x17, V1_19_4, V1_20)
                    .addMapping(0x18, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketClientConfiguration::new, builder()
                    .addMapping(0x15, V1_7_2, V1_8)
                    .addMapping(0x04, V1_9, V1_11_2)
                    .addMapping(0x05, V1_12)
                    .addMapping(0x04, V1_12_1, V1_13_2)
                    .addMapping(0x05, V1_14, V1_18_2)
                    .addMapping(0x08, V1_19_1)
                    .addMapping(0x07, V1_19_3)
                    .addMapping(0x08, V1_19_4, V1_20)
                    .addMapping(0x09, V1_20_2, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketTransaction::new, builder()
                    .addMapping(0x32, V1_7_2, V1_8)
                    .addMapping(0x11, V1_9, V1_12_2)
                    .addMapping(0x12, V1_13, V1_14_4)
                    .addMapping(0x13, V1_15, V1_15_2)
                    .addMapping(0x12, V1_16, V1_16_1)
                    .addMapping(0x11, V1_16_2, V1_16_4)
                    .addMapping(0x30, V1_17, V1_18_2)
                    .addMapping(0x2D, V1_19)
                    .addMapping(0x2F, V1_19_1)
                    .addMapping(0x2E, V1_19_3)
                    .addMapping(0x32, V1_19_4, V1_20)
                    .addMapping(0x33, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketTransaction::new, builder()
                    .addMapping(0x0F, V1_7_2, V1_8)
                    .addMapping(0x05, V1_9, V1_11_2)
                    .addMapping(0x06, V1_12)
                    .addMapping(0x05, V1_12_1, V1_12_2)
                    .addMapping(0x06, V1_13, V1_13_2)
                    .addMapping(0x07, V1_14, V1_16_2)
                    .addMapping(0x1D, V1_17, V1_18_2)
                    .addMapping(0x1F, V1_19)
                    .addMapping(0x20, V1_19_1)
                    .addMapping(0x1F, V1_19_3)
                    .addMapping(0x20, V1_19_4, V1_20)
                    .addMapping(0x23, V1_20_2)
                    .addMapping(0x24, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketAbilities::new, builder()
                    .addMapping(0x39, V1_7_2, V1_8)
                    .addMapping(0x2B, V1_9, V1_12)
                    .addMapping(0x2C, V1_12_1, V1_12_2)
                    .addMapping(0x2E, V1_13, V1_13_2)
                    .addMapping(0x31, V1_14, V1_14_4)
                    .addMapping(0x32, V1_15, V1_15_2)
                    .addMapping(0x31, V1_16, V1_16_1)
                    .addMapping(0x30, V1_16_2, V1_16_4)
                    .addMapping(0x32, V1_17, V1_18_2)
                    .addMapping(0x2f, V1_19)
                    .addMapping(0x31, V1_19_1)
                    .addMapping(0x30, V1_19_3)
                    .addMapping(0x34, V1_19_4, V1_20)
                    .addMapping(0x36, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketPosition::new, builder()
                    .addMapping(0x04, V1_7_2, V1_8)
                    .addMapping(0x0C, V1_9, V1_11_2)
                    .addMapping(0x0E, V1_12)
                    .addMapping(0x0D, V1_12_1, V1_12_2)
                    .addMapping(0x10, V1_13, V1_13_2)
                    .addMapping(0x11, V1_14, V1_15_2)
                    .addMapping(0x12, V1_16, V1_16_4)
                    .addMapping(0x11, V1_17, V1_18_2)
                    .addMapping(0x13, V1_19)
                    .addMapping(0x14, V1_19_1)
                    .addMapping(0x13, V1_19_3)
                    .addMapping(0x14, V1_19_4, V1_20)
                    .addMapping(0x16, V1_20_2)
                    .addMapping(0x17, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketPositionLook::new, builder()
                    .addMapping(0x08, V1_7_2, V1_8)
                    .addMapping(0x2E, V1_9, V1_12)
                    .addMapping(0x2F, V1_12_1, V1_12_2)
                    .addMapping(0x32, V1_13, V1_13_2)
                    .addMapping(0x35, V1_14, V1_14_4)
                    .addMapping(0x36, V1_15, V1_15_2)
                    .addMapping(0x35, V1_16, V1_16_1)
                    .addMapping(0x34, V1_16_2, V1_16_4)
                    .addMapping(0x38, V1_17, V1_18_2)
                    .addMapping(0x36, V1_19)
                    .addMapping(0x39, V1_19_1)
                    .addMapping(0x38, V1_19_3)
                    .addMapping(0x3C, V1_19_4, V1_20)
                    .addMapping(0x3E, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketPositionLook::new, builder()
                    .addMapping(0x06, V1_7_2, V1_8)
                    .addMapping(0x0D, V1_9, V1_11_2)
                    .addMapping(0x0F, V1_12)
                    .addMapping(0x0E, V1_12_1, V1_12_2)
                    .addMapping(0x11, V1_13, V1_13_2)
                    .addMapping(0x12, V1_14, V1_15_2)
                    .addMapping(0x13, V1_16, V1_16_4)
                    .addMapping(0x12, V1_17, V1_18_2)
                    .addMapping(0x14, V1_19)
                    .addMapping(0x15, V1_19_1)
                    .addMapping(0x14, V1_19_3)
                    .addMapping(0x15, V1_19_4, V1_20)
                    .addMapping(0x17, V1_20_2)
                    .addMapping(0x18, V1_20_3)
                    .getMapping()
            );
            clientbound.register(PacketChunk::new, builder()
                    .addMapping(0x21, V1_7_2, V1_8)
                    .addMapping(0x20, V1_9, V1_12_2)
                    .addMapping(0x22, V1_13, V1_13_2)
                    .addMapping(0x21, V1_14, V1_14_4)
                    .addMapping(0x22, V1_15, V1_15_2)
                    .addMapping(0x21, V1_16, V1_16_1)
                    .addMapping(0x20, V1_16_2, V1_16_4)
                    .addMapping(0x22, V1_17, V1_18_2)
                    .addMapping(0x1F, V1_19)
                    .addMapping(0x21, V1_19_1)
                    .addMapping(0x20, V1_19_3)
                    .addMapping(0x24, V1_19_4, V1_20)
                    .addMapping(0x25, V1_20_2, V1_20_3)
                    .getMapping()
            );
            serverbound.register(PacketTeleportConfirm::new, withSingle(VersionRange.of(V1_9, V1_20_3), 0x00));
            clientbound.register(PacketGameEvent::new, withSingle(VersionRange.of(V1_20_3), 0x20));
        }
    };

    public final LazyInit<ProtocolMappings> clientbound = new LazyInit<>(ProtocolMappings::new);
    public final LazyInit<ProtocolMappings> serverbound = new LazyInit<>(ProtocolMappings::new);
}
