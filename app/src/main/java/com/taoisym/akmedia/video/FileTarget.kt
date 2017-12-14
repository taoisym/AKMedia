package com.taoisym.akmedia.video

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.taoisym.akmedia.codec.*
import com.taoisym.akmedia.std.Supplier
import java.nio.ByteBuffer

class FileTarget(val next: IMediaSink<NioSegment>) : IMediaTargetSink<Unit, RealSurface>, IMediaSource<NioSegment, Unit> {

    private var input: Surface? = null
    private var enc: MediaCodec? = null
    private val info = MediaCodec.BufferInfo()
    private lateinit var outbuffers: Array<out ByteBuffer>
    private var memo = NioSegment(null)
    private var tracking: Boolean = false

    override val target = Supplier<RealSurface>()
    override fun prepare() {

    }

    override fun emit(data: Unit): Boolean {
        queryOutput()
        return true
    }

    override fun release() {
        flushOutput()
        next.release()
        input?.release()
        enc?.apply {
            stop()
            release()
        }
    }


    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        val hdfmt = SegmentFormat(format).align().format()
        hdfmt.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        try {
            enc = MediaCodec.createEncoderByType(format.mime)
            enc?.run {
                configure(hdfmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                input = createInputSurface()
                start()
                target.set(RealSurface(input!!, format.width, format.height))
                outbuffers = outputBuffers
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
        return Unit
    }

    override fun seek(pts: Long, flag: Int) {

    }

    override fun addSink(next: IMediaTargetSink<NioSegment, Unit>, flag: Int) = TODO()

    private fun queryOutput(): Boolean {
        var got = false
        val idx = enc?.dequeueOutputBuffer(info, 0) ?: -1
        if (idx >= 0) {
            if (tracking) {
                memo.set(info.presentationTimeUs, outbuffers[idx])
                memo.pos(0, outbuffers[idx].limit())
                memo.id = info.flags
                next.emit(memo)
                got = true
                outbuffers[idx].clear()
                //Log.e("onFrameAvailable", "output=" + info.presentationTimeUs);
            }
            enc?.releaseOutputBuffer(idx, false)
        } else if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            tracking = true
            next.setFormat(0, SegmentFormat(enc?.outputFormat))
        }
        return got
    }

    private fun flushOutput() {
        try {
            while (queryOutput()) {
            }
        } catch (e: Exception) {

        }
    }
}