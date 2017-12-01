package com.taoisym.akmedia.codec.avc

import android.media.MediaCodec
import com.taoisym.akmedia.codec.IMediaSink

import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat


class MediaWriter(private val muxer: MediaMuxer) : IMediaSink<NioSegment> {

    private var info = MediaCodec.BufferInfo()
    private var track: Int = 0

    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        track = muxer.onAddTrack(format.format())
        return null
    }

    override fun emit(nio: NioSegment): Boolean {
        info.presentationTimeUs = nio.pts
        info.offset = 0
        info.size = nio.buffer.limit()
        info.flags = nio.id
        /**
         * some codec output size=2 bad fragment
         */
        if (info.size > 4)
            muxer.writeSample(track, nio.buffer, info)
        return true
    }

    override fun release() {
        muxer.onEndTrack(track)
    }

    override fun seek(pts: Long, flag: Int) {

    }
}
