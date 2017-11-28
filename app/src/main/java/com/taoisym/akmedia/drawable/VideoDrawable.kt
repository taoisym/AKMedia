package com.taoisym.akmedia.drawable

import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import com.taoisym.akmedia.codec.AvcFileMeta
import com.taoisym.akmedia.codec.avc.MediaDecoder
import com.taoisym.akmedia.codec.avc.MediaSource
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.std.Lazy
import glm.vec2.Vec2

class VideoDrawable(val uri: String) : ExternalDrawable(0, 0), PlayAble {

    private lateinit var mDecoder: MediaSource

    override fun prepare(env: GLEnv) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val meta = AvcFileMeta(retriever)
        width = meta.width
        height = meta.height
        locShape = Loc(Vec2(0.0f, 0.0f), Vec2(1f, 1f))
        super.prepare(env)
    }


    override fun start() {
        val lazy = object : Lazy<SurfaceTexture>() {
            override fun refid() = input
        }

        mDecoder = MediaSource(MediaSource.CONTINUE, MediaSource.CONTINUE)
        mDecoder.addSink(MediaDecoder(null, lazy), 0)
        mDecoder.scatter(uri)
        mDecoder.start()

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
        super.release(env)
    }

    override fun seek(pts: Long) {
        mDecoder.seek(pts)
    }
}