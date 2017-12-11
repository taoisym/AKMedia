package com.taoisym.akmedia.render

import android.opengl.EGL14
import android.os.Handler
import android.os.HandlerThread
import com.taoisym.akmedia.render.egl.GLContext

class ResManager(env: GLEnv) {
    var resCtx: GLContext? = null
    val thread = object : HandlerThread("ResManager") {
        override fun onLooperPrepared() {
            super.onLooperPrepared()
            val handle = Handler(looper)
            resCtx = GLContext(null, GLContext.FLAG_TRY_GLES3)
            resCtx?.makeCurrent(EGL14.EGL_NO_SURFACE)
            env.mResourceHandle = handle
        }

        override fun run() {
            try {
                super.run()
            } finally {
                resCtx?.release()
            }
        }
    }

    init {
        thread.start()
    }


    fun release() {
        thread.quit()
    }
}