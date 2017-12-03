package com.taoisym.akmedia.drawable

import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.render.GLEnv
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread

class GifDrawable(val uri:String,bp:GifBitmapProvider):TextureDrawable(false,0,0),PlayAble
{
    val decorer= StandardGifDecoder(bp)
    var thread:Thread?=null
    var lock=java.lang.Object()
    var running=false
    override fun prepare(env: GLEnv) {
        decorer.read(BufferedInputStream(FileInputStream(uri)),
                File(uri).length().toInt())
        width=decorer.width
        height=decorer.height
        super.prepare(env)
        thread=thread {

            while (true) {
                synchronized(lock) {
                    while (running==false)
                        lock.wait()
                }
                try {
                    decorer.advance()
                    var frame=decorer.nextFrame
                    if(frame==null){
                        decorer.resetFrameIndex()
                    }else{
                        env.resManager?.upload(Runnable {
                            texture.value?.update(frame)
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

    override fun start() {
        synchronized(lock){
            running=true
            lock.notifyAll()
        }
    }

    override fun stop() {
        thread?.interrupt()
    }

    override fun pause() {
        synchronized(lock){
            running=false
            lock.notifyAll()
        }
    }

    override fun resume() {
        start()
    }
}