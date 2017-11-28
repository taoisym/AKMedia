package com.taoisym.akmedia.render.egl


import android.util.Pair
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.video.ResourceUploader


class GLEnv {
    lateinit var context: GLContext
    lateinit var oes: TextureRender
    lateinit var noes: TextureRender

    var glres:ResourceUploader?=null
    fun release() {
        context?.release()
    }

}
