package com.taoisym.akmedia.drawable

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.render.egl.GLTexture

open class TextureDrawable(oes: Boolean, var width: Int, var height: Int) : GLDrawable(oes) {
    override fun prepare(env: GLEnv) {
        super.prepare(env)
        texture = GLTexture(if (oes) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D, width, height)
        texture?.prepare(env)
    }

    override fun release(env: GLEnv) {
        super.release(env)
        texture?.release(env)
    }

}