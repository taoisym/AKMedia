package com.taoisym.akmedia.codec.recoder

import android.graphics.SurfaceTexture
import android.media.MediaExtractor
import android.os.Handler
import android.os.Looper

import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.avc.MediaDecoder
import com.taoisym.akmedia.codec.avc.MediaEncoder
import com.taoisym.akmedia.codec.avc.MediaMuxer
import com.taoisym.akmedia.codec.avc.MediaSource
import com.taoisym.akmedia.codec.avc.MediaWriter
import com.taoisym.akmedia.codec.chain.Passer

import java.io.IOException


class VideoRecoder : MediaConvertor {
    internal var `in`: String? = null
    internal var crop: MediaConvertor.VideoCropItem? = null
    internal var out: String
    internal var listener: MediaConvertor.ConvertListener? = null
    internal var uiThreaded = Handler(Looper.getMainLooper())
    internal var sender = Sender()

    constructor(`in`: String, out: String, listener: MediaConvertor.ConvertListener) {
        this.`in` = `in`
        this.out = out
        this.listener = listener
    }

    constructor(crop: MediaConvertor.VideoCropItem, out: String, listener: MediaConvertor.ConvertListener) {
        this.crop = crop
        this.out = out
        this.listener = listener
    }

    override fun start() {
        var muxer: MediaMuxer? = null
        try {
            muxer = MediaMuxer(out, 2)
        } catch (e: IOException) {
            e.printStackTrace()
            sendFinish(false)
            return
        }


        val decoder = MediaDecoder(MediaEncoder(MediaWriter(muxer)))
        /**
         * hook
         */
        val passer = object : Passer<NioSegment, SurfaceTexture>(decoder) {
            override fun prepare() {
                super.prepare()
                sendStart()
            }

            override fun emit(o: NioSegment): Boolean {
                sendProcess(o.pts)
                return super.emit(o)
            }

            override fun release() {
                super.release()
                sendFinish(true)
            }


        }
        val src = MediaSource(MediaSource.CONTINUE, MediaSource.STOP)
        src.addSink(MediaReader(MediaWriter(muxer)), 1)
        src.addSink(passer, 0)
        if (`in` != null) {
            src.emit(`in`!!)
        } else if (crop != null) {
            src.setSeekMode(MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            src.setPlayRange(crop!!.startPts, crop!!.endPts)
            src.emit(crop!!.uri)
        } else {

            return
        }
        src.start()
    }

    internal fun sendStart() {
        if (listener != null) {
            uiThreaded.post { listener!!.onStart() }
        }
    }

    internal fun sendProcess(pts: Long) {
        sender.send(pts)
    }

    internal fun sendFinish(success: Boolean) {
        if (listener != null) {
            uiThreaded.post { listener!!.onFinish(success) }
        }
    }

    internal inner class Sender : Runnable {
        var lastSend: Long = 0

        fun send(pts: Long) {
            //0.1 second
            if (pts - lastSend > 100000) {
                lastSend = pts
                uiThreaded.post(this)
            }
        }

        override fun run() {
            listener!!.onProcess(lastSend)
        }
    }
}
