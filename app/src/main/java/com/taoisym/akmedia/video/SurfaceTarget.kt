package com.taoisym.akmedia.video

import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.std.Supplier


/**
 * render for multi video
 */
class SurfaceTarget(surface: Supplier<RealSurface>) : IMediaTargetSink<Unit, RealSurface> {
    override val target = surface
    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    override fun scatter(unit: Unit): Boolean {
        return false
    }

    override fun release() {
    }


    override fun seek(pts: Long, flag: Int) {

    }
}