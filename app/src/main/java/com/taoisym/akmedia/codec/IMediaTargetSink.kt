package com.taoisym.akmedia.codec

import com.taoisym.akmedia.std.Supplier


interface IMediaTargetSink<Data, Target> {

    val format: SegmentFormat
        get() = TODO()
    val target: Supplier<Target>
        get() = TODO()

    fun prepare()

    fun setFormat(ctx: Any, format: SegmentFormat): Any?

    fun scatter(data: Data): Boolean

    fun release()

    fun seek(pts: Long, flag: Int) {}
}
typealias  IMediaSink<Data> = IMediaTargetSink<Data, Unit>

