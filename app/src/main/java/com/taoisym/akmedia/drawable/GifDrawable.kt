package com.taoisym.akmedia.drawable

import android.opengl.GLES20
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLTexture
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class GifDrawable(val uri: String, val bp: GifBitmapProvider) : GLDrawable(false), PlayAble {
    private val mCache = ArrayList<GLTexture>()
    private var start = false
    private lateinit var stop: () -> Unit
    override fun prepare(env: GLEnv) {
        super.prepare(env)
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

        val update: Runnable = object : Runnable {
            var idx = 0
            override fun run() {
                texture.value = mCache[idx]
                idx += 1
                if (idx == size)
                    idx = 0
                decorer.advance()
                env.postRender(this, decorer.nextDelay.toLong())
                start=true
            }
        }

        val ready = {
            env.postRender(update, 0)
        }
        stop = {
            env.removeRenderFunc(update)
        }

        ready?.invoke()
    }

    override fun release(env: GLEnv) {
        super.release(env)
        env.postResource {
            mCache.forEach { it.release(env) }
        }
    }

    override fun draw(env: GLEnv, render: TextureRender?, tr: GLTransform?) {
        if (start)
            super.draw(env, render, tr)
    }

    override fun start() {
        //start.invoke()
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