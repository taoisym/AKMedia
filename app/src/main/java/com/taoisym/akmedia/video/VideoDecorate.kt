package com.taoisym.akmedia.video

import android.content.Context
import android.graphics.BitmapFactory
import com.taoisym.akmedia.R
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.drawable.BitmapDrawable
import com.taoisym.akmedia.drawable.GifDrawable
import com.taoisym.akmedia.drawable.VideoDrawable
import com.taoisym.akmedia.layout.Loc
import glm.vec2.Vec2


/**
 * render for video
 */
class VideoDecorate(next: IMediaTargetSink<Unit, RealSurface>) : VideoGenerator(next) {
    var drawable: VideoDrawable? = null
    var bmp: BitmapDrawable? = null
    var bmp1: BitmapDrawable? = null
    var gif: GifDrawable?=null
    fun add(ctx: Context) {
        runGLThread {
            drawable = VideoDrawable("/sdcard/girl.mp4")
            drawable?.prepare(mEnv)
            drawable?.start()

            val b = BitmapFactory.decodeResource(ctx.resources, R.drawable.src)
            bmp = BitmapDrawable(b)
            bmp?.locShape = Loc(Vec2(-1, -1), Vec2(-0.5, -0.5))
            bmp?.prepare(mEnv)
            bmp1 = BitmapDrawable(b)
            bmp1?.locShape = Loc(Vec2(-0.5, -0.5), Vec2(0, 0))
            bmp1?.prepare(mEnv)
            gif= GifDrawable("/sdcard/gif.gif")
            gif?.locShape = Loc(Vec2(-1, -0.5), Vec2(-0.5, 0))
            gif?.prepare(mEnv)
        }
    }

    fun del() {
        runGLThread {
            drawable?.release(mEnv)
            drawable = null
            gif?.release(mEnv)
            gif=null
        }
    }

    override fun drawDecorate() {
        super.drawDecorate()
        drawable?.draw(mEnv)
        bmp?.draw(mEnv)
        bmp1?.draw(mEnv)
        gif?.draw(mEnv)
    }
}