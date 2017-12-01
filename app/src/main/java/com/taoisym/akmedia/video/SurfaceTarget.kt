package com.taoisym.akmedia.video

import android.opengl.GLES20
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.std.Supplier


/**
 * render for multi video
 */
class SurfaceTarget(val surface: Supplier<RealSurface>) : IMediaTargetSink<Unit, RealSurface> {
    override val target = surface

    override val format=SegmentFormat(0,0,0)


    override fun prepare() {
        val real=surface.get()
        format.width=real.width
        format.height=real.height
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    override fun forward(unit: Unit) {
        GLES20.glClearColor(0f,0f,0f,1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
    override fun emit(unit: Unit): Boolean {
        return false
    }

    override fun release() {
    }
    override fun seek(pts: Long, flag: Int) {
    }
}