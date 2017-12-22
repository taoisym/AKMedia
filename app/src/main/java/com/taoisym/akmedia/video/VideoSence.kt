package com.taoisym.akmedia.video

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import com.taoisym.akmedia.codec.*
import com.taoisym.akmedia.drawable.ExternalDrawable
import com.taoisym.akmedia.drawable.TextureDrawable
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLContext
import com.taoisym.akmedia.render.egl.GLFbo
import com.taoisym.akmedia.render.egl.GLToolkit
import com.taoisym.akmedia.std.Supplier


/**
 * render for mVideo
 */
open class VideoSence(private val next: IMediaTarget<Unit, RealSurface>) :
        IMediaSurfaceSink,
        IMediaSource<Unit, RealSurface> {

    private var mInputTarget = Supplier<SurfaceTexture>()

    lateinit var context: GLContext
    private lateinit var mFilterDrawable: TextureDrawable
    private lateinit var mFilterRender: TextureRender

    private var mEglThread: HandlerThread? = null
    private var mGLHandle: Handler? = null
    protected lateinit var mEnv: GLEnv

    private lateinit var mInFormat: SegmentFormat
    private lateinit var mOutFormat: SegmentFormat

    private lateinit var mMainOutput: OutputNode
    private var mSubOutput: OutputNode? = null

    override val format: SegmentFormat
        get() = mOutFormat
    override val target: Supplier<SurfaceTexture>
        get() = mInputTarget

    override fun prepare() {
        mEglThread = EglThread()
    }

    fun setFilter(render: TextureRender) {
        mEnv.postResource {
            render.prepare(mEnv)
            mFilterRender?.release(mEnv)
            mFilterRender = render
        }
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        mEnv = ctx as GLEnv
        mInFormat = format
        mEglThread?.start()
        return true
    }

    override fun emit(unit: NioSegment): Boolean {
        return false
    }


    override fun release() {
        mEglThread?.quitSafely()
        mEglThread = null
    }

    override fun seek(pts: Long, flag: Int) = throw UnsupportedOperationException()


    override fun addSink(sink: IMediaTarget<Unit, RealSurface>, flag: Int) {
        mEnv.postRender {
            mSubOutput = OutputNode(sink).init(context)
        }

    }

    override fun delSink(pass: IMediaTarget<Unit, RealSurface>) {
        mEnv.postRender {
            mSubOutput?.release()
            mSubOutput = null
        }
    }

    private inner class OutputNode(val next: IMediaTarget<Unit, RealSurface>) {
        lateinit var mSurfaceCanvans: GLContext.EglSurface
        var mPtsStart = 0L
        lateinit var mSurface: RealSurface

        fun init(GLContext: GLContext): OutputNode {
            next.prepare()
            next.setFormat(mEnv, mOutFormat)
            mSurface = next.target.get()
            mSurfaceCanvans = GLContext.createWindowSurface(mSurface.surface)
            return this
        }

        fun release() {
            mSurfaceCanvans.release()
            next.release()
        }

        fun makeCurrent() {
            mSurfaceCanvans.makeCurrent()
        }

        fun swapdraw(render: TextureRender, timestamp: Long) {
            if (mPtsStart == 0L) {
                mPtsStart = timestamp
            }
            mSurfaceCanvans.makeCurrent()
            GLToolkit.checkError()
            GLES20.glViewport(0, 0, mOutFormat.width, mOutFormat.height)
            next.forward(Unit)
            mFilterDrawable.draw(mEnv, render, null)
            mSurfaceCanvans.setPresentationTime(timestamp - mPtsStart)
            mSurfaceCanvans.swap()
            next.emit(Unit)
        }
    }

    inner class EglThread : HandlerThread("VideoSence") {
        private lateinit var mSrcDrawable: ExternalDrawable
        private lateinit var mCahceDrawable: TextureDrawable
        private lateinit var mCacheFbo: GLFbo
        private lateinit var mFilterFbo: GLFbo
        private lateinit var mOesRender: TextureRender
        private lateinit var mTexRender: TextureRender
        override fun onLooperPrepared() {
            super.onLooperPrepared()
            mGLHandle = Handler(this.looper)
            prepareInGL()
        }

        override fun run() {
            try {
                super.run()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    mEnv?.let {
                        releaseInGL(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        private fun prepareInGL() {
            val env = mEnv
            var rotate = false
            if (mInFormat.rotation == 90 || mInFormat.rotation == 180) {
                val swap = mInFormat.width
                mInFormat.width = mInFormat.height
                mInFormat.height = swap
                rotate = true
            }
            mOutFormat = SegmentFormat(mInFormat)
            mOutFormat.height = mOutFormat.width
            mOutFormat.rotation = 0

            context = GLToolkit.eglSetup(env.resManager?.resCtx?.context, true)

            mEnv.mRenderHandle = mGLHandle!!
            mMainOutput = OutputNode(next).init(context)
            mMainOutput.makeCurrent()

            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            mOesRender = TextureRender(true)
            mTexRender = TextureRender(false)
            mFilterRender = TextureRender(false)

            env.oes = mOesRender
            env.tex = mTexRender
            mOesRender.prepare(env)
            mTexRender.prepare(env)
            mFilterRender.prepare(env)

            mSrcDrawable = ExternalDrawable(mInFormat.width, mInFormat.height)
            mSrcDrawable.locTex = Loc(mInFormat.dir, rotate, mInFormat.height * 1f / mInFormat.width
                    / (mOutFormat.height * 1.0f / mOutFormat.width))

            mCahceDrawable = TextureDrawable(false, mOutFormat.width, mOutFormat.height)
            mFilterDrawable = TextureDrawable(false, mOutFormat.width, mOutFormat.height)


            mSrcDrawable.prepare(env)
            mCahceDrawable.prepare(env)
            mFilterDrawable.prepare(env)

            mCacheFbo = GLFbo(mCahceDrawable.texture.value)
            mCacheFbo.prepare(env)

            mFilterFbo = GLFbo(mFilterDrawable.texture.value)
            mFilterFbo.prepare(env)

            mInputTarget.set(mSrcDrawable.target.get())
            mSrcDrawable.target.get()?.setOnFrameAvailableListener {
                eglDrawFrame()
            }
        }

        private fun releaseInGL(context: GLEnv) {
            mOesRender.release(context)
            mTexRender.release(context)
            mFilterRender.release(context)
            mSrcDrawable.release(context)
            mSubOutput?.release()
            mMainOutput.release()
            mEnv.release()
        }

        private fun eglDrawFrame() {
            drawCache()
            //filter
            drawFilter()
            val time = mSrcDrawable.target.get()?.timestamp ?: 0
            mMainOutput.apply {
                swapdraw(mTexRender, time)
            }
            mSubOutput?.apply {
                swapdraw(mTexRender, time)
            }

        }

        fun drawCache() {
            mMainOutput.makeCurrent()
            mCacheFbo.using(true)
            mOesRender.clearColor(floatArrayOf(1.0f, 1f, 1f, 1f))
            GLES20.glViewport(0, 0, mOutFormat.width, mOutFormat.height)
            val mtx = FloatArray(16)
            mInputTarget.get().getTransformMatrix(mtx)
            mSrcDrawable.draw(mEnv, mOesRender, null)
            drawDecorate()
            mCacheFbo.using(false)

        }

        private fun drawFilter() {
            mFilterFbo.using(true)
            GLES20.glViewport(0, 0, mOutFormat.width, mOutFormat.height)
            mCahceDrawable.draw(mEnv, mFilterRender, null)
            mFilterFbo.using(false)
        }
    }

    open fun drawDecorate() {

    }
}