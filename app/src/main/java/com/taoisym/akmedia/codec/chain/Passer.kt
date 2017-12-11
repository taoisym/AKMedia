package com.taoisym.akmedia.codec.chain

import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.IMediaSource
import com.taoisym.akmedia.codec.IMediaTarget
import com.taoisym.akmedia.codec.SegmentFormat


open class Passer<Input,Target>(internal var next: IMediaTarget<Input,Target>) : IMediaTarget<Input,Target>, IMediaSource<Input, Target> {
    override fun addSink(pass: IMediaTarget<Input, Target>, flag: Int) {

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

    override fun emit(o: Input): Boolean {
        return next.emit(o)
    }

    override fun release() {
        next.release()
    }


    override fun seek(pts: Long, flag: Int) {
        next.seek(pts, flag)
    }


}
