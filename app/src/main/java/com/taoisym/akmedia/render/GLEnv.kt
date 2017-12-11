package com.taoisym.akmedia.render


import android.os.Handler


class GLEnv {

    lateinit var oes: TextureRender
    lateinit var tex: TextureRender
    lateinit var mRenderHandle: Handler
    lateinit var mResourceHandle: Handler
    var resManager: ResManager

    fun postRender(fn: Runnable, delay: Long = 0) {
        mRenderHandle.postDelayed(fn, delay)
    }

    fun postResource(fn: Runnable, delay: Long = 0) {
        mResourceHandle.postDelayed(fn, delay)
    }

    constructor() {
        resManager = ResManager(this)
    }

    fun release() {
        resManager?.release()
    }

    fun postRender(fn: () -> Unit) {
        postRender(object : Runnable {
            override fun run() {
                fn()
            }
        }, 0)
    }

    fun postResource(fn: () -> Unit) {
        postResource(object : Runnable {
            override fun run() {
                fn()
            }
        }, 0)
    }

    fun removeRenderFunc(func: Runnable) {
        mRenderHandle.removeCallbacks(func)
    }
}
