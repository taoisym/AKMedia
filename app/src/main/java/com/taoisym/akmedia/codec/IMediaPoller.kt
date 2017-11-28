package com.taoisym.akmedia.codec

import com.taoisym.akmedia.codec.avc.MediaSource


interface IMediaPoller<Next> {
    val format: SegmentFormat
        get() = throw RuntimeException()

    @Throws(MediaSource.MediaException::class)
    fun setFormat(ctx: Any, format: SegmentFormat): Any?

    fun <Front> setFront(poller: IMediaPoller<Front>)

    @Throws(InterruptedException::class)
    fun pull(timeout: Long): Next

    fun release()

    fun feedback(delay: Long)
}
