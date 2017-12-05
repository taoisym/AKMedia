package com.taoisym.akmedia.video

import android.opengl.GLES20
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.std.Supplier


/**
 * render for multi video
 */
class SurfaceTarget(val surface: Supplier<RealSurface>) : IMediaTargetSink<Unit, RealSurface> {
    override val target = surface
    private var paddingBottom:Int=0
    private var paddingTop:Int=0
    override var format=SegmentFormat(0,0,0)
    val transform=GLTransform()
    override fun prepare() {
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        this.format=SegmentFormat(format)
        val real=surface.get()
        paddingBottom =real.height-format.height
        transform.setWindowLayout(real.width,real.height,paddingTop,paddingBottom)

        return null
    }

    override fun forward(unit: Unit) {
        GLES20.glClearColor(0f,0f,0f,1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glViewport(0, paddingBottom,format.width, format.height)

    }
    override fun emit(unit: Unit): Boolean {
        return false
    }

    override fun release() {
    }
    override fun seek(pts: Long, flag: Int) {
    }
}