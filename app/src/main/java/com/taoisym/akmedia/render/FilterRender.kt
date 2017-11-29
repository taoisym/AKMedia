package com.taoisym.akmedia.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.render.egl.GLProgram
import com.taoisym.akmedia.render.egl.GLTexture
import com.taoisym.akmedia.render.egl.IGLNode
import com.taoisym.akmedia.std.Ref

class FilterRender(vs:String,fs:String,val bmp:Bitmap) : TextureRender(vs,fs) {
    var texture_1=0
    var filterTexture= Ref<GLTexture>(null)
    override fun prepare(env: GLEnv) {
        super.prepare(env)
        texture_1=GLES20.glGetUniformLocation(id, "texture_1")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        val tex=GLTexture(GLES20.GL_TEXTURE_2D,bmp.width,bmp.height)
        tex.prepare(env)
        tex.update(bmp)
        filterTexture.value=tex
    }

    override fun using(use: Boolean) {
        super.using(use)
        if(use) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, filterTexture.value!!.id)
        }else{
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
        }
    }
}