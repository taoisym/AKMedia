package com.taoisym.akmedia.drawable

import android.opengl.GLES20
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.egl.GLTexture
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.concurrent.thread

class GifDrawable0(val uri:String, bp:GifBitmapProvider):TextureDrawable(false,0,0),PlayAble
{
    val decorer= StandardGifDecoder(bp)
    var running=false
    val cache=ArrayList<GLTexture>()
    override fun prepare(env: GLEnv) {
        decorer.read(BufferedInputStream(FileInputStream(uri)),
                File(uri).length().toInt())
        width=decorer.width
        height=decorer.height
        super.prepare(env)
        env.resManager?.upload(Runnable {
            while (true) {
                decorer.advance()
                var frame = decorer.nextFrame
                if (frame == null) {
                    break
                } else {
                    val tex=GLTexture(GLES20.GL_TEXTURE_2D,width,height)
                    tex.prepare(env)
                    tex.update(frame)
                    cache.add(tex)
                }
            }
        })
        val size=decorer.frameCount

        val update=object :Runnable {
            var idx=0
            override fun run() {
                texture.value=cache[idx]
                idx=idx+1
                if(idx==size)
                    idx=0
                env.handle.postDelayed(this,decorer.nextDelay.toLong())
            }
        }
        env.resManager?.upload(update)
    }

    override fun release(env: GLEnv) {
        super.release(env)
    }

    override fun start() {
    }

    override fun stop() {
    }

    override fun pause() {
    }

    override fun resume() {
        start()
    }
}