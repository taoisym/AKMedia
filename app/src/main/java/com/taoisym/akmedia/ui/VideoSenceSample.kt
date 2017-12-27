package com.taoisym.akmedia.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.R
import com.taoisym.akmedia.codec.IMediaTarget
import com.taoisym.akmedia.drawable.BitmapDrawable
import com.taoisym.akmedia.drawable.GifDrawable
import com.taoisym.akmedia.drawable.VideoDrawable
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.layout.Pos
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.video.RealSurface
import com.taoisym.akmedia.video.VideoSence


/**
 * render for mVideo
 */
class VideoSenceSample(next: IMediaTarget<Unit, RealSurface>) : VideoSence(next) {
    var mVideo: VideoDrawable? = null
    var mBmp0: BitmapDrawable? = null
    var mGif0: GifDrawable? = null
    var mGif1: GifDrawable? = null
    var mGif3: GifDrawable? = null

    val mGbp = GifBitmapProvider(LruBitmapPool(10))

    fun add(ctx: Context) {
        //todo fix here
        mEnv.postRender {
            var video: VideoDrawable? = null
            var bmp0: BitmapDrawable
            var gif0: GifDrawable
            var gif1: GifDrawable
            var gif3: GifDrawable


            val render = TextureRender(true, true)
            render.apply {
                prepare(mEnv)
                video = VideoDrawable("/sdcard/girl.mp4", this)
                video?.locShape = Loc(Pos(0.0f, 0.0f), Pos(1f, 1f))
                video?.prepare(mEnv)
            }


            val s1 = BitmapFactory.decodeResource(ctx.resources, R.raw.s1)
            bmp0 = BitmapDrawable(s1)
            bmp0.locShape = Loc(Pos(-0.5f, -1f), Pos(0f, -0.5f))
            bmp0.prepare(mEnv)
            gif0 = GifDrawable("/sdcard/2.gif", mGbp)
            gif0.locShape = Loc(Pos(-1f, -0.5f), Pos(-0.5f, 0f))
            gif0.prepare(mEnv)
            gif1 = GifDrawable("/sdcard/3.gif", mGbp)
            gif1.locShape = Loc(Pos(-0.5f, -0.5f), Pos(0f, 0f))
            gif1.prepare(mEnv)
            gif3 = GifDrawable("/sdcard/1.gif", mGbp)
            gif3.locShape = Loc(Pos(-1f, -1f), Pos(-0.5f, -0.5f))
            gif3.prepare(mEnv)
            GLES20.glFinish()
            mEnv.postRender {
                mGif3 = gif3
                mBmp0 = bmp0
                mVideo = video
                mVideo?.start()
                mGif0 = gif0
                mGif1 = gif1
            }
        }
    }

    fun del() {
        mEnv.postRender {
            mVideo?.release(mEnv)
            mBmp0?.release(mEnv)
            mBmp0?.release(mEnv)
            mGif0?.release(mEnv)
            mGif1?.release(mEnv)
            mEnv.postRender {
                mVideo = null
                mBmp0 = null
                mGif3 = null
                mGif1 = null
                mGif0 = null
            }
        }
    }

    override fun drawDecorate() {
        super.drawDecorate()
        mVideo?.draw(mEnv, null, null)
        mBmp0?.draw(mEnv, null, null)
        mGif0?.draw(mEnv, null, null)
        mGif1?.draw(mEnv, null, null)
        mGif3?.draw(mEnv, null, null)

    }
}