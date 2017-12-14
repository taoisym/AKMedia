package com.taoisym.akmedia.drawable

import android.graphics.SurfaceTexture
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender

open class ExternalDrawable(width: Int, height: Int) : TextureDrawable(true, width, height) {
    var input: SurfaceTexture? = null
        get() = field
        private set(value) {
            field = value
        }

    override fun prepare(env: GLEnv) {
        super.prepare(env)
        if(texture.value!=null)
            input = SurfaceTexture(texture.value!!.id)
    }

    override fun draw(env: GLEnv, render: TextureRender?, tr: GLTransform?) {
        input?.run {
            updateTexImage()
            super.draw(env, render,null )
        }
    }

    override fun release(env: GLEnv) {
        super.release(env)
        input?.release()
        input = null
    }
}