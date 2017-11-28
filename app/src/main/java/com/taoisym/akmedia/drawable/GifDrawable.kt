package com.taoisym.akmedia.drawable

import com.bumptech.glide.gifdecoder.GifDecoder
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.render.egl.GLEnv
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread

class GifDrawable(val uri:String):TextureDrawable(false,0,0){
    val decorer= StandardGifDecoder(GifBitmapProvider(LruBitmapPool(6)))
    var thread:Thread?=null;
    override fun prepare(env: GLEnv) {
        decorer.read(BufferedInputStream(FileInputStream(uri)),
                File(uri).length().toInt())
        width=decorer.width
        height=decorer.height
        super.prepare(env)
        thread=thread {
            while (true){
                try {
                    decorer.advance()
                    var frame=decorer.nextFrame
                    if(frame==null){
                        decorer.resetFrameIndex()
                    }else{
                        env.glres?.upload(Runnable {
                            texture?.update(frame)
                        })
                        Thread.sleep(decorer.nextDelay.toLong())
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun release(env: GLEnv) {
        super.release(env)
        thread?.interrupt()
    }
}