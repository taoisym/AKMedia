package com.taoisym.akmedia.codec.chain

import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.IMediaSource
import com.taoisym.akmedia.codec.Segment
import com.taoisym.akmedia.codec.SegmentFormat


class PassNext<T : Segment<*>>(internal var sink0: IMediaSink<T>, internal var sink1: IMediaSink<T>) : IMediaSink<T>, IMediaSource<T, Unit> {

    override fun prepare() {
        sink0.prepare()
        sink1.prepare()
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        sink0.setFormat(ctx, format)
        sink1.setFormat(ctx, format)
        return null
    }

    fun setState(state: Int, pts: Long) {

    }

    override fun emit(data: T): Boolean {
        sink0.emit(data)
        data.reset()
        sink1.emit(data)
        data.reset()
        return true
    }

    override fun release() {
        sink0.release()
        sink1.release()
    }


    override fun seek(pts: Long, flag: Int) {
        sink0.seek(pts, flag)
        sink1.seek(pts, flag)
    }

    override fun addSink(sink: IMediaSink<T>, flag: Int) {

    }
}
