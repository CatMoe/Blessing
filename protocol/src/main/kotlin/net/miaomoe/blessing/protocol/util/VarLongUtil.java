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

package net.miaomoe.blessing.protocol.util;

import io.netty.buffer.ByteBuf;

public class VarLongUtil {
    protected static void writeVarLong(final ByteBuf byteBuf, final long value) {
        if ((value & 0xFFFFFFFFFFFFFF80L) == 0L) {
            byteBuf.writeByte((byte) value);
        } else if ((value & 0xFFFFFFFFFFFFC000L) == 0L) {
            int w = (int) ((value & 0x7FL | 0x80L) << 8 | value >>> 7);
            byteBuf.writeShort(w);
        } else {
            writeVarLongFull(byteBuf, value);
        }
    }

    protected static void writeVarLongFull(final ByteBuf byteBuf, final long value) {
        if ((value & 0xFFFFFFFFFFFFFF80L) == 0L) {
            byteBuf.writeByte((byte) value);
        } else if ((value & 0xFFFFFFFFFFFFC000L) == 0L) {
            int w = (int) ((value & 0x7FL | 0x80L) << 8 | value >>> 7);
            byteBuf.writeShort(w);
        } else if ((value & 0xFFFFFFFFFFE00000L) == 0L) {
            int w = (int) ((value & 0x7FL | 0x80L) << 16 | (value >>> 7 & 0x7FL | 0x80L) << 8 | value >>> 14);
            byteBuf.writeMedium(w);
        } else if ((value & 0xFFFFFFFFF0000000L) == 0L) {
            int w =
                    (int) ((value & 0x7FL | 0x80L) << 24 | (value >>> 7 & 0x7FL | 0x80L) << 16 | (value >>> 14 & 0x7FL | 0x80L) << 8 | value >>> 21);
            byteBuf.writeInt(w);
        } else {
            long l =
                    (value & 0x7FL | 0x80L) << 24 | (value >>> 7 & 0x7FL | 0x80L) << 16 | (value >>> 14 & 0x7FL | 0x80L) << 8 | (value >>> 21 & 0x7FL | 0x80L);
            if ((value & 0xFFFFFFF800000000L) == 0L) {
                int w =
                        (int) l;
                byteBuf.writeInt(w);
                byteBuf.writeByte((int) (value >>> 28));
            } else if ((value & 0xFFFFFC0000000000L) == 0L) {
                int w =
                        (int) l;
                int w2 = (int) ((value >>> 28 & 0x7FL | 0x80L) << 8 | value >>> 35);
                byteBuf.writeInt(w);
                byteBuf.writeShort(w2);
            } else if ((value & 0xFFFE000000000000L) == 0L) {
                int w =
                        (int) l;
                int w2 = (int) ((value >>> 28 & 0x7FL | 0x80L) << 16 | (value >>> 35 & 0x7FL | 0x80L) << 8 | value >>> 42);
                byteBuf.writeInt(w);
                byteBuf.writeMedium(w2);
            } else {
                long w =
                        (value & 0x7FL | 0x80L) << 56 | (value >>> 7 & 0x7FL | 0x80L) << 48 | (value >>> 14 & 0x7FL | 0x80L) << 40 | (value >>> 21 & 0x7FL | 0x80L) << 32 | (value >>> 28 & 0x7FL | 0x80L) << 24 | (value >>> 35 & 0x7FL | 0x80L) << 16 | (value >>> 42 & 0x7FL | 0x80L) << 8 | value >>> 49;
                if ((value & 0xFF00000000000000L) == 0L) {
                    byteBuf.writeLong(w);
                } else if ((value & Long.MIN_VALUE) == 0L) {
                    byteBuf.writeLong(w);
                    byteBuf.writeByte((byte) (value >>> 56));
                } else {
                    int w2 = (int) ((value >>> 56 & 0x7FL | 0x80L) << 8 | value >>> 63);
                    byteBuf.writeLong(w);
                    byteBuf.writeShort(w2);
                }
            }
        }
    }
}
