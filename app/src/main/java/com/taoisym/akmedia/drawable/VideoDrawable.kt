package com.taoisym.akmedia.drawable

import android.media.MediaMetadataRetriever
import com.taoisym.akmedia.codec.AvcFileMeta
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.codec.audio.AacPlayer
import com.taoisym.akmedia.codec.avc.MediaDecoder
import com.taoisym.akmedia.codec.avc.MediaSource
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender

class VideoDrawable(val uri: String, val custom: TextureRender) : ExternalDrawable(0, 0), PlayAble {
    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    override fun release() {
    }

    private var mDecoder: MediaSource? = null

    override fun prepare(env: GLEnv) {
        super.prepare(env)
        env.postRender {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(uri)
            val meta = AvcFileMeta(retriever)
            width = meta.width
            height = meta.height
            super.prepare(env)


            val decoder = MediaSource(MediaSource.CONTINUE, MediaSource.CONTINUE)
            decoder.addSink(MediaDecoder(this), 0)
            decoder.addSink(MediaDecoder(AacPlayer()), 1)
            decoder.emit(uri)
            mDecoder = decoder
        }
    }


    override fun start() {
        mDecoder?.start()

    }

    override fun draw(env: GLEnv, render: TextureRender?, tr: GLTransform?) {
        super.draw(env, custom, tr)
    }

    override fun stop() {
        mDecoder?.stop()

    }

    override fun pause() {
        mDecoder?.pause()
    }

    override fun resume() {
        mDecoder?.resume()
    }

    override fun release(env: GLEnv) {
        //mDecoder.stop()
        custom.release(env)
        super.release(env)
    }

    override fun seek(pts: Long) {
        mDecoder?.seek(pts)
    }
}