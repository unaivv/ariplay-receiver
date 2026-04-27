package com.example.airplayreceiverunai

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class AirPlayDecoder(private val surface: Surface) {

    private var codec: MediaCodec? = null
    private var configured = false

    fun onVideoData(data: ByteArray, isH265: Boolean) {
        if (!configured) {
            tryConfigureFromAnnexB(data, isH265)
        }
        if (configured) feedAnnexB(data)
    }

    private fun tryConfigureFromAnnexB(data: ByteArray, isH265: Boolean) {
        val nals = splitAnnexB(data)

        var sps: ByteArray? = null
        var pps: ByteArray? = null

        for (nal in nals) {
            if (nal.isEmpty()) continue
            val nalType = nal[0].toInt() and 0x1F
            when (nalType) {
                7 -> sps = nal
                8 -> pps = nal
            }
        }

        if (!isH265 && (sps == null || pps == null)) return

        val mime = if (isH265) MediaFormat.MIMETYPE_VIDEO_HEVC else MediaFormat.MIMETYPE_VIDEO_AVC
        val fmt = MediaFormat.createVideoFormat(mime, 1920, 1080)

        if (!isH265 && sps != null && pps != null) {
            val startCode = byteArrayOf(0, 0, 0, 1)
            fmt.setByteBuffer("csd-0", ByteBuffer.wrap(startCode + sps))
            fmt.setByteBuffer("csd-1", ByteBuffer.wrap(startCode + pps))
        }

        try {
            codec = MediaCodec.createDecoderByType(mime).also {
                it.configure(fmt, surface, null, 0)
                it.start()
            }
            configured = true
            Log.i(TAG, "MediaCodec configured: ${if (isH265) "H.265" else "H.264"} sps=${sps?.size} pps=${pps?.size}")
        } catch (e: Exception) {
            Log.e(TAG, "MediaCodec configure failed", e)
        }
    }

    // Split Annex B stream into NAL units (without start codes)
    private fun splitAnnexB(data: ByteArray): List<ByteArray> {
        val nals = mutableListOf<ByteArray>()
        var start = -1
        var i = 0
        while (i <= data.size - 3) {
            if (data[i] == 0.toByte() && data[i + 1] == 0.toByte()) {
                if (i + 3 < data.size && data[i + 2] == 0.toByte() && data[i + 3] == 1.toByte()) {
                    if (start >= 0) nals.add(data.copyOfRange(start, i))
                    start = i + 4; i += 4; continue
                } else if (data[i + 2] == 1.toByte()) {
                    if (start >= 0) nals.add(data.copyOfRange(start, i))
                    start = i + 3; i += 3; continue
                }
            }
            i++
        }
        if (start >= 0 && start < data.size) nals.add(data.copyOfRange(start, data.size))
        return nals
    }

    private fun feedAnnexB(data: ByteArray) {
        val c = codec ?: return
        val idx = c.dequeueInputBuffer(10_000L)
        if (idx < 0) return

        val buf = c.getInputBuffer(idx) ?: return
        buf.clear()
        val len = minOf(data.size, buf.remaining())
        buf.put(data, 0, len)
        c.queueInputBuffer(idx, 0, len, System.nanoTime() / 1_000L, 0)

        val info = MediaCodec.BufferInfo()
        val out = c.dequeueOutputBuffer(info, 0L)
        if (out >= 0) c.releaseOutputBuffer(out, true)
    }

    fun release() {
        runCatching { codec?.stop() }
        runCatching { codec?.release() }
        codec = null
        configured = false
    }

    companion object { private const val TAG = "AirPlayDecoder" }
}
