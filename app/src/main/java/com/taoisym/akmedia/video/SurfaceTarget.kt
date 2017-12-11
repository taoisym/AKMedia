package com.taoisym.akmedia.video

import android.opengl.GLES20
import com.taoisym.akmedia.codec.IMediaTarget
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.std.Supplier


/**
 * render for multi mVideo
 */
class SurfaceTarget(val surface: Supplier<RealSurface>) : IMediaTarget<Unit, RealSurface> {
    override val target = surface
    private var paddingBottom:Int=0
    private var paddingTop:Int=0
    override var format=SegmentFormat(0,0,0)
    val transform=GLTransform()
    override fun prepare() {
    }

    override fun setFormat(ctx: Any, informat: SegmentFormat): Any? {
        this.format=SegmentFormat(informat)
        val real=surface.get()
        val height=(real.width* informat.height*1.0/ informat.width).toInt()
        paddingBottom =real.height-height

        format.width=real.width
        format.height=height
        transform.setWindowLayout(real.width,height,paddingTop,paddingBottom)

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