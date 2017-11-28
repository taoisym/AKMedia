package com.taoisym.akmedia.codec.avc

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log

import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MediaMuxer @Throws(IOException::class)
constructor(uri: String, internal val trackNumber: Int) {
    protected var muxer: android.media.MediaMuxer
    protected var ready = AtomicBoolean()
    protected var tracks = AtomicInteger()

    init {
        muxer = android.media.MediaMuxer(uri, android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        //muxer.setOrientationHint(90);
    }

    fun writeSample(track: Int, byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (ready.get()) {
            muxer.writeSampleData(track, byteBuffer, info)
        }
    }

    fun onAddTrack(outputFormat: MediaFormat): Int {
        val track = muxer.addTrack(outputFormat)
        if (tracks.incrementAndGet() == trackNumber) {
            muxer.start()
            ready.set(true)
        }
        return track
    }

    fun onEndTrack(track: Int) {
        Log.e("onFrameAvailable", "onEndTrack")
        if (tracks.decrementAndGet() == 0) {

            ready.set(false)
            //No Need Call Stop, For Bug Track
            try {
                muxer.release()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}

