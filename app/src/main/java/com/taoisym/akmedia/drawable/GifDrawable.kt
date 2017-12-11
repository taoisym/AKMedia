package com.taoisym.akmedia.drawable

import android.opengl.GLES20
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.egl.GLTexture
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class GifDrawable(val uri: String, val bp: GifBitmapProvider) : GLDrawable(false), PlayAble {
    private val mCache = ArrayList<GLTexture>()
    private lateinit var start: () -> Unit
    private lateinit var stop: () -> Unit
    override fun prepare(env: GLEnv) {
        super.prepare(env)
        env.postResource {
            val decorer = StandardGifDecoder(bp)
            decorer.read(BufferedInputStream(FileInputStream(uri)),
                    File(uri).length().toInt())
            val width = decorer.width
            val height = decorer.height
            val size = decorer.frameCount

            (0 until size).forEach {
                decorer.advance()
                var frame = decorer.nextFrame
                val tex = GLTexture(GLES20.GL_TEXTURE_2D, width, height)
                tex.prepare(env)
                tex.update(frame)
                mCache.add(tex)
            }
            decorer.resetFrameIndex()
            var idx = 0
            val update: Runnable = object : Runnable {
                override fun run() {
                    texture.value = mCache[idx]
                    idx = idx + 1
                    if (idx == size)
                        idx = 0
                    decorer.advance()
                    env.postRender(this, decorer.nextDelay.toLong())
                }
            }
            start = {
                env.postRender(update, 0)
            }
            stop = {
                env.removeRenderFunc(update)
            }
        }
    }

    override fun release(env: GLEnv) {
        super.release(env)
        env.postResource {
            mCache.forEach { it.release(env) }
        }
    }

    override fun start() {
        start.invoke()
    }

    override fun stop() {
        stop.invoke()
        texture.value = null
    }

    override fun pause() {
        stop.invoke()
    }

    override fun resume() {
        start()
    }
}