package com.taoisym.akmedia.render

import android.opengl.EGL14
import android.os.Handler
import android.os.HandlerThread
import com.taoisym.akmedia.render.egl.GLContext

class ResManager(env: GLEnv) {
    private var handle: Handler?=null
    private var resCtx:GLContext?=null
    val thread = object : HandlerThread("ResManager") {
        override fun onLooperPrepared() {
            super.onLooperPrepared()
            handle=Handler(looper)
            env.context.let {
                resCtx = GLContext(it.context, 0)
                resCtx?.makeCurrent(EGL14.EGL_NO_SURFACE)
            }
        }

        override fun run() {
            try {
                super.run()
            }finally {
                resCtx?.release()
            }
        }
    }
    init {
        thread.start()
    }

    fun post(fn: Runnable) {
        handle?.post(fn)
    }
    fun postDelay(fn: Runnable,delay:Long) {
        handle?.postDelayed(fn,delay)
    }

    fun release() {
        thread.quit()
    }
}