package com.taoisym.akmedia.drawable

import android.opengl.GLES20
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.egl.GLTexture
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class GifDrawable0(val uri: String,val bp: GifBitmapProvider) : TextureDrawable(false, 0, 0), PlayAble {

    val cache = ArrayList<GLTexture>()
    override fun prepare(env: GLEnv) {
        val decorer = StandardGifDecoder(bp)
        decorer.read(BufferedInputStream(FileInputStream(uri)),
                File(uri).length().toInt())
        width = decorer.width
        height = decorer.height
        val size = decorer.frameCount
        //assume frame rate is fixed
        super.prepare(env)
        val update = object : Runnable {
            var idx = 0
            override fun run() {
                texture.value = cache[idx]
                idx = idx + 1
                if (idx == size)
                    idx = 0
                decorer.advance()
                env.handle.postDelayed(this, decorer.nextDelay.toLong())
            }
        }
        env.resManager?.post(Runnable {
            for (i in 0..size-1) {
                decorer.advance()
                var frame = decorer.nextFrame
                val tex = GLTexture(GLES20.GL_TEXTURE_2D, width, height)
                tex.prepare(env)
                tex.update(frame)
                cache.add(tex)
            }
            decorer.resetFrameIndex()
            env.resManager?.post(update)
        })
    }

    override fun release(env: GLEnv) {
        super.release(env)
        cache.forEach { it.release(env) }
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