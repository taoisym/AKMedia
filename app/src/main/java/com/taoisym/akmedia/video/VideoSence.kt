package com.taoisym.akmedia.video

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import com.taoisym.akmedia.codec.IMediaSource
import com.taoisym.akmedia.codec.IMediaTargetSink
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.drawable.ExternalDrawable
import com.taoisym.akmedia.drawable.TextureDrawable
import com.taoisym.akmedia.layout.GLTransform
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.ResManager
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLContext
import com.taoisym.akmedia.render.egl.GLFbo
import com.taoisym.akmedia.render.egl.GLToolkit
import com.taoisym.akmedia.std.Supplier


/**
 * render for video
 */
open class VideoSence(private val next: IMediaTargetSink<Unit, RealSurface>) :
        IMediaTargetSink<Unit, SurfaceTexture>,
        IMediaSource<Unit, RealSurface> {

    private var mInputTarget = Supplier<SurfaceTexture>()

    private lateinit var mFilterDrawable: TextureDrawable
    private lateinit var mFilterRender: TextureRender

    private var mEglThread: HandlerThread? = null
    private var mGLHandle: Handler? = null
    protected lateinit var mEnv: GLEnv
    protected lateinit var mTranform: GLTransform

    private lateinit var mInFormat: SegmentFormat
    private lateinit var mOutFormat: SegmentFormat

    private lateinit var mMainOutput: OutputNode
    private var mSubOutput: OutputNode? = null

    override val format: SegmentFormat
        get() = mOutFormat
    override val target: Supplier<SurfaceTexture>
        get() = mInputTarget

    override fun prepare() {
    }

    fun setFilter(render: TextureRender) {
        runGLThread {
            render.prepare(mEnv)
            mFilterRender?.release(mEnv)
            mFilterRender = render
        }
    }





    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        mEnv = ctx as GLEnv
        mInFormat=format
        mEglThread = EglThread()
        mEglThread?.start()
        return true
    }

    override fun emit(unit: Unit): Boolean {
        return false
    }


    override fun release() {
        mEglThread?.quitSafely()
        mEglThread=null
    }

    override fun seek(pts: Long, flag: Int) = throw UnsupportedOperationException()

    fun runGLThread(fn: () -> Unit) {
        mGLHandle?.post {
            fn()
        }
    }

    override fun addSink(sink: IMediaTargetSink<Unit, RealSurface>, flag: Int) {
        runGLThread {
            mSubOutput = OutputNode(sink).init(mEnv.context)
        }

    }

    override fun delSink(pass: IMediaTargetSink<Unit, RealSurface>) {
        runGLThread {
            mSubOutput?.release()
            mSubOutput = null
        }
    }

    private inner class OutputNode(val next: IMediaTargetSink<Unit, RealSurface>) {
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

        fun swapdraw(render:TextureRender,timestamp: Long) {
            if (mPtsStart == 0L) {
                mPtsStart = timestamp
            }
            mSurfaceCanvans.makeCurrent()
            GLToolkit.checkError()
            GLES20.glViewport(0, 0, mOutFormat.width, mOutFormat.height)
            next.forward(Unit)
            mFilterDrawable.draw(mEnv, render,null )
            mSurfaceCanvans.setPresentationTime(timestamp - mPtsStart)
            mSurfaceCanvans.swap()
            next.emit(Unit)
        }
    }
    inner class EglThread:HandlerThread("VideoSence"){
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

                }

            }
        }
        private fun prepareInGL() {
            val env=mEnv
            if (mInFormat.rotation == 90 || mInFormat.rotation == 180) {
                val swap = mInFormat.width
                mInFormat.width = mInFormat.height
                mInFormat.height = swap
            }
            mOutFormat =SegmentFormat(mInFormat)
            mOutFormat.height=mOutFormat.width
            mOutFormat.rotation=0

            val eglContext = GLToolkit.eglSetup(null, true)
            mEnv.context = eglContext
            mEnv.resManager = ResManager(mEnv)
            mEnv.handle= mGLHandle!!
            mMainOutput = OutputNode(next).init(eglContext)
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
            mSrcDrawable.locTex= Loc(mInFormat.dir,true,mInFormat.height*1f/mInFormat.width
                    /(mOutFormat.height*1.0f/mOutFormat.width))

            mCahceDrawable = TextureDrawable(false, mOutFormat.width, mOutFormat.height)
            mFilterDrawable = TextureDrawable(false, mOutFormat.width, mOutFormat.height)


            mSrcDrawable.prepare(env)
            mCahceDrawable.prepare(env)
            mFilterDrawable.prepare(env)

            mCacheFbo = GLFbo(mCahceDrawable.texture.value)
            mCacheFbo.prepare(env)

            mFilterFbo = GLFbo(mFilterDrawable.texture.value)
            mFilterFbo.prepare(env)

            mInputTarget.set(mSrcDrawable.input)
            mSrcDrawable.input?.setOnFrameAvailableListener {
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
            val time = mSrcDrawable.input?.timestamp ?: 0
            mMainOutput.apply {
                swapdraw(mTexRender,time)
            }
            mSubOutput?.apply {
                swapdraw(mTexRender,time)
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
            mCahceDrawable.draw(mEnv, mFilterRender,null )
            mFilterFbo.using(false)
        }
    }
    open fun drawDecorate() {

    }
}