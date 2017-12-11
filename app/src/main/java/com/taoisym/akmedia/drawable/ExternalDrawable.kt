package com.taoisym.akmedia.drawable

import com.taoisym.akmedia.codec.IMediaSurfaceSink
import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender

open class ExternalDrawable(width: Int, height: Int) : TextureDrawable(true, width, height), IMediaSurfaceSink {
    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    override fun release() {
    }

    override fun emit(data: NioSegment): Boolean {
        return false
    }

    override fun draw(env: GLEnv, render: TextureRender?, tr: GLTransform?) {
        target.value()?.run {
            updateTexImage()
            super.draw(env, render, null)
        }
    }

    override fun release(env: GLEnv) {
        super.release(env)
        target.value()?.release()
    }
}