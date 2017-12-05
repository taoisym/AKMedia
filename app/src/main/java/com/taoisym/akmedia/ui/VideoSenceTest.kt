package com.taoisym.akmedia.ui

import android.content.Context
import android.graphics.BitmapFactory
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.R
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.drawable.BitmapDrawable
import com.taoisym.akmedia.drawable.GifDrawable0
import com.taoisym.akmedia.drawable.VideoDrawable
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.layout.Pos
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.video.RealSurface
import com.taoisym.akmedia.video.VideoSence


/**
 * render for video
 */
class VideoSenceTest(next: IMediaTargetSink<Unit, RealSurface>) : VideoSence(next) {
    var video: VideoDrawable? = null
    var bmp: BitmapDrawable? = null
    var bmp1: BitmapDrawable? = null
    var gif: GifDrawable0?=null
    var gif1: GifDrawable0?=null
    val bp= GifBitmapProvider(LruBitmapPool(10))

    fun add(ctx: Context) {
        runGLThread {
            val custom=TextureRender(true,true)
            custom.apply {
                prepare(mEnv)
                video = VideoDrawable("/sdcard/girl.mp4",this)
                video?.locShape = Loc(Pos(0.0f, 0.0f), Pos(1f, 1f))

                video?.prepare(mEnv)
                video?.start()
            }


            val b = BitmapFactory.decodeResource(ctx.resources,R.raw.src)
            bmp = BitmapDrawable(b)
            bmp?.locShape = Loc(Pos(-1f, -1f), Pos(-0.5f, -0.5f))
            bmp?.prepare(mEnv)
            bmp1 = BitmapDrawable(b)
            bmp1?.locShape = Loc(Pos(-0.5f, -1f), Pos(0f, -0.5f))
            bmp1?.prepare(mEnv)
            gif= GifDrawable0("/sdcard/gif.gif",bp)
            gif?.locShape = Loc(Pos(-1f, -0.5f), Pos(-0.5f, 0f))
            gif?.prepare(mEnv)
            gif1= GifDrawable0("/sdcard/gif.gif",bp)
            gif1?.locShape = Loc(Pos(-0.5f, -0.5f), Pos(0f, 0f))
            gif1?.prepare(mEnv)

            gif?.start()
            gif1?.start()
        }
    }

    fun del() {
        runGLThread {
            video?.release(mEnv)
            video = null
            bmp?.release(mEnv)
            bmp=null
            bmp1?.release(mEnv)
            bmp1=null
            gif?.release(mEnv)
            gif=null
            gif1?.release(mEnv)
            gif1=null
        }
    }

    override fun drawDecorate() {
        super.drawDecorate()
        video?.draw(mEnv, null,null )
        bmp?.draw(mEnv, null, null)
        bmp1?.draw(mEnv, null, null)
        gif?.draw(mEnv, null, null)
        gif1?.draw(mEnv, null,null )
    }
}