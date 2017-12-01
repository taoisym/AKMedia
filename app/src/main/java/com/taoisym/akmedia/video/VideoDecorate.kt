package com.taoisym.akmedia.video

import android.content.Context
import android.graphics.BitmapFactory
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.taoisym.akmedia.R
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.drawable.BitmapDrawable
import com.taoisym.akmedia.drawable.GifDrawable
import com.taoisym.akmedia.drawable.VideoDrawable
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.render.TextureRender
import glm.vec2.Vec2


/**
 * render for video
 */
class VideoDecorate(next: IMediaTargetSink<Unit, RealSurface>) : VideoGenerator(next) {
    var video: VideoDrawable? = null
    var bmp: BitmapDrawable? = null
    var bmp1: BitmapDrawable? = null
    var gif: GifDrawable?=null
    var gif1: GifDrawable?=null
    val bp= GifBitmapProvider(LruBitmapPool(6))

    fun add(ctx: Context) {
        runGLThread {
            val custom=TextureRender(true,true)
            custom.apply {
                prepare(mEnv)
                video = VideoDrawable("/sdcard/girl.mp4",this)
                video?.locShape = Loc(Vec2(0.0f, 0.0f), Vec2(1f, 1f))

                video?.prepare(mEnv)
                video?.start()
            }


            val b = BitmapFactory.decodeResource(ctx.resources,R.raw.src)
            bmp = BitmapDrawable(b)
            bmp?.locShape = Loc(Vec2(-1, -1), Vec2(-0.5, -0.5))
            bmp?.prepare(mEnv)
            bmp1 = BitmapDrawable(b)
            bmp1?.locShape = Loc(Vec2(-0.5, -1), Vec2(0, -0.5))
            bmp1?.prepare(mEnv)
            gif= GifDrawable("/sdcard/gif.gif",bp)
            gif?.locShape = Loc(Vec2(-1, -0.5), Vec2(-0.5, 0))
            gif?.prepare(mEnv)
            gif1= GifDrawable("/sdcard/gif.gif",bp)
            gif1?.locShape = Loc(Vec2(-0.5, -0.5), Vec2(0, 0))
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