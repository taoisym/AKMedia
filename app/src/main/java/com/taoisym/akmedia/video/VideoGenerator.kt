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
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLContext
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.render.egl.GLFbo
import com.taoisym.akmedia.render.egl.GLToolkit
import com.taoisym.akmedia.std.Supplier


/**
 * render for video
 */
open class VideoGenerator(private val next: IMediaTargetSink<Unit, RealSurface>) :
        IMediaTargetSink<Unit, SurfaceTexture>,
        IMediaSource<Unit, RealSurface> {

    private lateinit var mSrcDrawable: ExternalDrawable
    private lateinit var mCahceDrawable: TextureDrawable
    private lateinit var mFilterDrawable: TextureDrawable
    private var mInputTarget = Supplier<SurfaceTexture>()

    private lateinit var mCacheFbo: GLFbo
    private lateinit var mFilterFbo: GLFbo
    private lateinit var mOesRender: TextureRender
    private lateinit var mTexRender: TextureRender
    private lateinit var mFilterRender: TextureRender
    private var mEglThread: HandlerThread? = null
    private var mGLHanlde: Handler? = null
    protected lateinit var mEnv: GLEnv
    private lateinit var mFormat: SegmentFormat

    private lateinit var mMainOutput: OutputNode
    private var mSubOutput: OutputNode? = null

    override val format: SegmentFormat
        get() = mFormat
    override val target: Supplier<SurfaceTexture>
        get() = mInputTarget

    private fun releaseInGL(context: GLEnv) {
        mOesRender.release(context)
        mTexRender.release(context)
        mFilterRender.release(context)
        mSrcDrawable.release(context)
        mSubOutput?.release()
        mMainOutput.release()
        mEnv.release()
    }

    fun setFilter(render: TextureRender) {
        runGLThread {
            render.prepare(mEnv)
            mFilterRender?.release(mEnv)
            mFilterRender = render
        }
    }

    private fun prepareInGL(env: GLEnv, format: SegmentFormat) {
        mEnv = env
        if (format.rotation == 90 || format.rotation == 180) {
            val swap = format.width
            format.width = format.height
            format.height = swap
        }
        mFormat = format

        val eglContext = GLToolkit.eglSetup(null, true)
        env.context = eglContext
        val res = ResourceUploader(mGLHanlde!!)
        env.glres = res

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

        mSrcDrawable = ExternalDrawable(format.width, format.height)
        mSrcDrawable.locTex.rotation = format.rotation

        mCahceDrawable = TextureDrawable(false, format.width, format.height)
        mCahceDrawable.locTex.mirror = false

        mFilterDrawable = TextureDrawable(false, format.width, format.height)
        mFilterDrawable.locTex.mirror = false


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


    private fun eglDrawFrame() {

        drawCache()
        //filter
        drawFilter()
        val time = mSrcDrawable.input?.timestamp ?: 0
        mMainOutput.apply {
            swapdraw(time)
        }
        mSubOutput?.apply {
            swapdraw(time)
        }

    }

    fun drawCache() {
        mMainOutput.makeCurrent()
        mCacheFbo.using(true)
        mOesRender.clearColor(floatArrayOf(1.0f, 1f, 1f, 1f))
        GLES20.glViewport(0, 0, format.width, format.height)
        mSrcDrawable.draw(mEnv, mOesRender)
        drawDecorate()
        mCacheFbo.using(false)

    }

    private fun drawFilter() {
        mFilterFbo.using(true)
        GLES20.glViewport(0, 0, format.width, format.height)
        mCahceDrawable.draw(mEnv, mFilterRender)
        mFilterFbo.using(false)
    }

    open fun drawDecorate() {

    }


    override fun prepare() {
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        mEnv = ctx as GLEnv
        mEglThread = object : HandlerThread(this.javaClass.simpleName) {
            override fun onLooperPrepared() {
                super.onLooperPrepared()
                mGLHanlde = Handler(this.looper)
                prepareInGL(mEnv, format)
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

        }
        mEglThread?.start()
        return null
    }

    override fun scatter(unit: Unit): Boolean {
        return false
    }


    override fun release() {
        mEglThread?.quit()
    }

    override fun seek(pts: Long, flag: Int) = throw UnsupportedOperationException()

    fun runGLThread(fn: () -> Unit) {
        mGLHanlde?.post(Runnable {
            fn()
        })
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
            next.setFormat(mEnv, format)
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

        fun swapdraw(timestamp: Long) {
            if (mPtsStart == 0L) {
                mPtsStart = timestamp
            }
            mSurfaceCanvans.makeCurrent()
            GLToolkit.checkError()
            GLES20.glViewport(0, 0, mSurface.width, mSurface.height)
            mFilterDrawable.draw(mEnv, mTexRender)
            mSurfaceCanvans.setPresentationTime(timestamp - mPtsStart)
            mSurfaceCanvans.swap()
            next.scatter(Unit)
        }

    }
}