package com.taoisym.akmedia.drawable

import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import com.taoisym.akmedia.codec.AvcFileMeta
import com.taoisym.akmedia.codec.avc.MediaDecoder
import com.taoisym.akmedia.codec.avc.MediaSource
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.std.Lazy

class VideoDrawable(val uri: String,val custom: TextureRender) : ExternalDrawable(0, 0), PlayAble {

    private lateinit var mDecoder: MediaSource

    override fun prepare(env: GLEnv) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val meta = AvcFileMeta(retriever)
        width = meta.width
        height = meta.height
        super.prepare(env)

        val lazy = object : Lazy<SurfaceTexture>() {
            override fun refid() = input
        }
        mDecoder = MediaSource(MediaSource.CONTINUE, MediaSource.CONTINUE)
        mDecoder.addSink(MediaDecoder(null, lazy), 0)
        mDecoder.emit(uri)
    }


    override fun start() {
        mDecoder.start()

    }

    override fun draw(env: GLEnv, render: TextureRender?, tr: GLTransform?) {
        super.draw(env, custom, tr)
    }

    override fun stop() {
        mDecoder.stop()

    }

    override fun pause() {
        mDecoder.pause()
    }

    override fun resume() {
        mDecoder.resume()
    }

    override fun release(env: GLEnv) {
        //mDecoder.stop()
        custom.release(env)
        super.release(env)
    }

    override fun seek(pts: Long) {
        mDecoder.seek(pts)
    }
}