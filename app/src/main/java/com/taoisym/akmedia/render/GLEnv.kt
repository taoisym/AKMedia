package com.taoisym.akmedia.render


import com.taoisym.akmedia.render.egl.GLContext


class GLEnv {
    lateinit var context: GLContext
    lateinit var oes: TextureRender
    lateinit var tex: TextureRender
    var resManager: ResManager?=null
    fun release() {
        context?.release()
        resManager?.release()
    }
}
