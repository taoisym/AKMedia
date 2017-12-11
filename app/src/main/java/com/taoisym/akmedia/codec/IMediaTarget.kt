package com.taoisym.akmedia.codec

import android.graphics.SurfaceTexture
import com.taoisym.akmedia.std.Supplier


interface IMediaTarget<Input, Target> {

    val format: SegmentFormat
        get() = TODO()
    val target: Supplier<Target>
        get() = TODO()

    fun prepare()

    fun setFormat(ctx: Any, format: SegmentFormat): Any?
    fun forward(data: Input){}
    fun emit(data: Input): Boolean
    fun release()
    fun seek(pts: Long, flag: Int) {}
}
typealias  IMediaSurfaceSink = IMediaTarget<NioSegment, SurfaceTexture>

typealias  IMediaSink<Input> = IMediaTarget<Input, Unit>

