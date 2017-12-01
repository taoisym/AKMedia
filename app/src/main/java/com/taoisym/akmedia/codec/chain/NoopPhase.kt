package com.taoisym.akmedia.codec.chain

import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.SegmentFormat


class NoopPhase<T> : IMediaSink<T> {


    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    override fun emit(o: T): Boolean {
        return false
    }

    override fun release() {

    }

}
