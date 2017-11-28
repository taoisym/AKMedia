package com.taoisym.akmedia.codec.chain

import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.IMediaSource
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.codec.SegmentFormat


open class Passer<T>(internal var next: IMediaSink<T>) : IMediaSink<T>, IMediaSource<T, Unit> {
    override fun addSink(pass: IMediaTargetSink<T, Unit>, flag: Int) {

    }

    override val format: SegmentFormat
        get() = next.format


    override fun prepare() {
        next.prepare()
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        next.setFormat(ctx, format)
        return null
    }

    override fun scatter(o: T): Boolean {
        return next.scatter(o)
    }

    override fun release() {
        next.release()
    }


    override fun seek(pts: Long, flag: Int) {
        next.seek(pts, flag)
    }


}
