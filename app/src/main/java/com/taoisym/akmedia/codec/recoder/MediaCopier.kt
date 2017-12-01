package com.taoisym.akmedia.codec.recoder

import android.media.MediaCodec
import android.media.MediaExtractor
import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.codec.avc.MediaMuxer
import java.nio.ByteBuffer


open class MediaCopier(private val muxer: MediaMuxer) : IMediaSink<NioSegment> {
    protected var extractor: MediaExtractor? = null
    protected var info = MediaCodec.BufferInfo()
    protected var ptsOffset: Long = 0
    private var buffer: ByteBuffer? = null
    private var track = -1

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        this.extractor = ctx as MediaExtractor
        buffer = ByteBuffer.allocateDirect(format.MAX_INPUT_SIZE)
        if (track < 0)
            track = muxer.onAddTrack(format.format())
        return null
    }

    override fun emit(nio: NioSegment): Boolean {
        buffer?.run {
            buffer!!.clear()
            val size = extractor!!.readSampleData(buffer!!, 0)
            val flag = extractor!!.sampleFlags
            info.flags = flag
            info.presentationTimeUs = nio.pts
            info.offset = 0
            info.size = size
            muxer.writeSample(track, buffer!!, info)
            return true
        }
        return false
    }

    fun fuckHuaWeiPush(nio: NioSegment): Boolean {
        buffer?.apply {
            clear()
            if (extractor != null && extractor!!.sampleTrackIndex < 0) {
                return false
            }
            val extr = extractor!!
            val size = extr.readSampleData(buffer!!, 0)
            val pts = extr.sampleTime
            val flag = extr.sampleFlags
            info.flags = flag
            info.presentationTimeUs = pts
            info.offset = 0
            info.size = size + 8
            clear()
            position(size)
            put(ByteArray(8))
            flip()
            muxer.writeSample(track, buffer!!, info)
        }
        return true
    }

    override fun release() {
        muxer.onEndTrack(track)
    }

    override fun seek(pts: Long, flag: Int) {

    }

    override fun prepare() {

    }
}
