package com.taoisym.akmedia.drawable

import android.graphics.SurfaceTexture
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLEnv

open class ExternalDrawable(width: Int, height: Int) : TextureDrawable(true, width, height) {
    var input: SurfaceTexture? = null
        get() = field
        private set(value) {
            field = value
        }

    override fun prepare(env: GLEnv) {
        super.prepare(env)
        texture?.run {
            input = SurfaceTexture(id)
        }
    }

    override fun draw(env: GLEnv,render: TextureRender?) {
        input?.run {
            updateTexImage()
            super.draw(env,render)
        }
    }

    override fun release(env: GLEnv) {
        super.release(env)
        input?.release()
        input = null
    }
}