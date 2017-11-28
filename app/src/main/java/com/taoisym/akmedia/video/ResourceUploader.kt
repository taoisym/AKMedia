package com.taoisym.akmedia.video

import android.os.Handler
import android.os.HandlerThread
import com.taoisym.akmedia.render.egl.GLContext

/**
 * Created by taoisym on 17-11-28.
 */
class ResourceUploader(val handle:Handler){
    fun upload(fn:Runnable){
        handle.post(fn)
    }
        fun release() {

        }
}
/**
 * current  not work
 */
//class ResourceUploader {
//    private var handle: Handler?=null
//    private var context: GLContext?=null
//    constructor(context: GLContext) {
//        this.context=context
//        thread.start()
//    }
//
//    val thread = object : HandlerThread("ResourceUploader") {
//        override fun onLooperPrepared() {
//            super.onLooperPrepared()
//            handle=Handler(looper)
//            context?.let {
//                context = GLContext(it.context, 0)
//            }
//        }
//
//        override fun run() {
//            super.run()
//            context?.release()
//        }
//    }
//
//    fun upload(fn: Runnable) {
//        handle?.post(fn)
//    }
//
//    fun release() {
//        thread.quit()
//    }
//}