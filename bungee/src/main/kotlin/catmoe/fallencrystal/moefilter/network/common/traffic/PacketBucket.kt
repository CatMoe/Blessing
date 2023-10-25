/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
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

// https://github.com/Spottedleaf/PacketLimiter/blob/master/src/main/java/ca/spottedleaf/packetlimiter/PacketBucket.java
package catmoe.fallencrystal.moefilter.network.common.traffic

import java.util.*
@Suppress("MemberVisibilityCanBePrivate")
class PacketBucket(
    val intervalTime: Double,
    val totalBuckets: Int
) {
    var intervalResolution = 0.0
    private var data: IntArray
    private var newestData = 0
    private var lastBucketTime = 0.0
    private var sum = 0

    init {
        intervalResolution = intervalTime / totalBuckets.toDouble()
        data = IntArray(totalBuckets)
    }

    fun incrementPackets(packets: Int): Int {
        return this.incrementPackets(System.nanoTime(), packets)
    }

    private fun incrementPackets(currentTime: Long, packets: Int): Int {
        val timeMs = currentTime * NANOSECONDS_TO_MILLISECONDS
        var timeDelta = timeMs - lastBucketTime
        if (timeDelta < 0.0) {
            // we presume the difference is small. nano time always moves forward
            timeDelta = 0.0
        }
        if (timeDelta < intervalResolution) {
            data[newestData] += packets
            return packets.let { sum += it; sum }
        }
        val bucketsToMove = (timeDelta / intervalResolution).toInt()
        val nextBucketTime = lastBucketTime + bucketsToMove * intervalResolution
        if (bucketsToMove >= totalBuckets) {
            // we need to simply clear all data
            Arrays.fill(data, 0)
            data[0] = packets
            sum = packets
            newestData = 0
            lastBucketTime = timeMs
            return packets
        }

        for (i in 1 until bucketsToMove) {
            val index = (newestData + i) % totalBuckets
            sum -= data[index]
            data[index] = 0
        }
        val newestDataIndex = (newestData + bucketsToMove) % totalBuckets
        sum += packets - data[newestDataIndex]
        data[newestDataIndex] = packets
        newestData = newestDataIndex
        lastBucketTime = nextBucketTime
        return sum
    }

    fun getTotalPackets(): Int {
        return sum
    }

    fun getCurrentPacketRate(): Double {
        return sum / (intervalTime / MILLISECONDS_TO_SECONDS.toDouble())
    }

    companion object {
        private const val NANOSECONDS_TO_MILLISECONDS = 1.0e-6 // 1e3 / 1e9

        private const val MILLISECONDS_TO_SECONDS = 1000
    }
}