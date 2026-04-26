package com.example.airplayreceiverunai

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class AirPlayDecoder(private val surface: Surface) {

    private var codec: MediaCodec? = null
    private var configured = false
    private var sps: ByteArray? = null
    private var pps: ByteArray? = null

    fun onVideoData(data: ByteArray, isH265: Boolean) {
        if (!configured) {
            extractSpsPps(data, isH265)
            if (isH265 || (sps != null && pps != null)) {
                configure(isH265)
            }
        }
        if (configured) feedAvcc(data)
    }

    private fun extractSpsPps(data: ByteArray, isH265: Boolean) {
        if (isH265) return // H.265 CSD handled differently; skip for now
        var i = 0
        while (i + 4 <= data.size) {
            val nalLen = readInt32(data, i); i += 4
            if (nalLen <= 0 || i + nalLen > data.size) break
            val nalType = data[i].toInt() and 0x1F
            when (nalType) {
                7 -> sps = data.copyOfRange(i, i + nalLen)
                8 -> pps = data.copyOfRange(i, i + nalLen)
            }
            i += nalLen
        }
    }

    private fun configure(isH265: Boolean) {
        val mime = if (isH265) MediaFormat.MIMETYPE_VIDEO_HEVC else MediaFormat.MIMETYPE_VIDEO_AVC
        val fmt  = MediaFormat.createVideoFormat(mime, 1920, 1080)
        if (!isH265) {
            sps?.let { fmt.setByteBuffer("csd-0", ByteBuffer.wrap(it)) }
            pps?.let { fmt.setByteBuffer("csd-1", ByteBuffer.wrap(it)) }
        }
        try {
            codec = MediaCodec.createDecoderByType(mime).also {
                it.configure(fmt, surface, null, 0)
                it.start()
            }
            configured = true
            Log.i(TAG, "MediaCodec configured for ${if (isH265) "H.265" else "H.264"}")
        } catch (e: Exception) {
            Log.e(TAG, "MediaCodec configure failed", e)
        }
    }

    private fun feedAvcc(data: ByteArray) {
        val c = codec ?: return
        val idx = c.dequeueInputBuffer(10_000L)
        if (idx < 0) return

        val buf = c.getInputBuffer(idx) ?: return
        buf.clear()
        annexBFromAvcc(data, buf)
        c.queueInputBuffer(idx, 0, buf.position(), System.nanoTime() / 1_000L, 0)

        val info = MediaCodec.BufferInfo()
        val out  = c.dequeueOutputBuffer(info, 0L)
        if (out >= 0) c.releaseOutputBuffer(out, true)
    }

    private fun annexBFromAvcc(avcc: ByteArray, out: ByteBuffer) {
        val start = byteArrayOf(0, 0, 0, 1)
        var i = 0
        while (i + 4 <= avcc.size) {
            val nalLen = readInt32(avcc, i); i += 4
            if (nalLen <= 0 || i + nalLen > avcc.size) break
            out.put(start)
            out.put(avcc, i, nalLen)
            i += nalLen
        }
    }

    private fun readInt32(buf: ByteArray, off: Int): Int =
        ((buf[off].toInt() and 0xFF) shl 24) or
        ((buf[off + 1].toInt() and 0xFF) shl 16) or
        ((buf[off + 2].toInt() and 0xFF) shl 8) or
        (buf[off + 3].toInt() and 0xFF)

    fun release() {
        runCatching { codec?.stop() }
        runCatching { codec?.release() }
        codec = null
        configured = false
        sps = null; pps = null
    }

    companion object { private const val TAG = "AirPlayDecoder" }
}
