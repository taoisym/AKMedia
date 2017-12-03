package com.taoisym.akmedia.render


import android.os.Handler
import com.taoisym.akmedia.render.egl.GLContext


class GLEnv {
    lateinit var context: GLContext
    lateinit var oes: TextureRender
    lateinit var tex: TextureRender
    lateinit var handle: Handler
    var resManager: ResManager?=null
    fun release() {
        context?.release()
        resManager?.release()
    }
}
