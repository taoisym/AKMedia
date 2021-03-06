package com.taoisym.akmedia.codec.recoder

import android.media.MediaExtractor
import com.taoisym.akmedia.codec.*
import java.nio.ByteBuffer


class MediaReader(private var next: IMediaSink<NioSegment>) : IMediaSurfaceSink, IMediaSource<NioSegment, Unit> {
    private lateinit var extractor: MediaExtractor
    private var buffer: ByteBuffer? = null
    private var fmt: SegmentFormat? = null


    override fun prepare() {
        next.prepare()
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        extractor = ctx as MediaExtractor
        fmt = SegmentFormat(format)
        if (next != null) {
            next.setFormat(ctx, format)
        }
        buffer = ByteBuffer.allocateDirect(format.MAX_INPUT_SIZE)
        return null
    }

    override fun emit(nioSegment: NioSegment): Boolean {
        buffer!!.clear()
        val size = extractor.readSampleData(buffer!!, 0)
        val pts = extractor.sampleTime
        val flag = extractor.sampleFlags
        val memo = NioSegment(fmt)
        memo.set(pts, buffer)
        memo.pos(0, size)
        return next.emit(memo)
    }

    override fun addSink(sink: IMediaSink<NioSegment>, flag: Int) {
        this.next = sink
    }

    override fun release() {
        next.release()
    }


    override fun seek(pts: Long, flag: Int) {

    }
}
