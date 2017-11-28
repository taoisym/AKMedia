package com.taoisym.akmedia.codec.recoder

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.Matrix
import android.os.Build
import android.os.Bundle
import android.os.ConditionVariable
import android.os.Handler
import android.view.Surface
import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.IMediaSource
import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.drawable.GLDrawable
import com.taoisym.akmedia.drawable.TextureDrawable
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLContext
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.render.egl.GLToolkit
import java.nio.ByteBuffer


class RedrawEncoder : IMediaSink<PresentSegment>, IMediaSource<NioSegment, Unit> {

    private var mUseWidthLimit: Boolean = false
    private var mUseHeightLimit: Boolean = false
    private var mHeightLimit: Int = 0
    private var mWidthLimit: Int = 0
    internal var next: IMediaSink<NioSegment>
    internal var encoder: MediaCodec? = null
    internal var tracking: Boolean = false
    private var output: Array<ByteBuffer>? = null
    private val info = MediaCodec.BufferInfo()

    //video only
    private var inputSurface: SurfaceTexture? = null
    private var encoderSurface: Surface? = null
    private var encHandle: Handler? = null

    internal var param = Bundle()
    internal var memo = NioSegment(null)
    private var ptsBuffer: FrameBuffer? = null
    private val surfaceWaitor = ConditionVariable(false)
    private val waitNewFrame = ConditionVariable(true)


    internal var env: GLEnv? = null
    internal var context: GLContext? = null
    internal var out: GLContext.EglSurface? = null
    internal var render: TextureRender? = null
    internal var view: GLDrawable? = null


    val outputTexture: SurfaceTexture?
        get() {
            surfaceWaitor.block()
            return inputSurface
        }

    constructor(next: IMediaSink<NioSegment>, encHandle: Handler) {
        this.next = next
        this.encHandle = encHandle
        param.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
    }

    constructor(next: IMediaSink<NioSegment>, encHandle: Handler, widthLimit: Int, heightLimit: Int) {
        this.next = next
        this.encHandle = encHandle
        param.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)

        if (widthLimit != 0) {
            mWidthLimit = widthLimit
            mUseWidthLimit = true
        }

        if (heightLimit != 0) {
            mHeightLimit = heightLimit
            mUseHeightLimit = true
        }
    }

    override fun prepare() {
        next.prepare()
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        if (encoder != null) {
            flushOutput()
            return null
        }
        val fmt = format.format()
        //--
        val fmt1 = SegmentFormat(fmt)
        fmt.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        try {
            encoder = MediaCodec.createEncoderByType(format.mime)
            encoder!!.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoderSurface = encoder!!.createInputSurface()
            createInputSurface(fmt1.width, fmt1.height)
            encoder!!.start()
            output = encoder!!.outputBuffers
            ptsBuffer = FrameBuffer(output!!.size)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

        return null
    }

    internal fun forceKeyFrame() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            encoder!!.setParameters(param)
        }
    }

    override fun scatter(data: PresentSegment): Boolean {
        ptsBuffer!!.write(data.pts, data.data())
        //must block continue draw
        waitNewFrame()
        return true
    }

    internal fun waitNewFrame() {
        waitNewFrame.block()
        waitNewFrame.close()
    }

    internal fun queryOutput(): Boolean {
        var got = false
        val idx = encoder!!.dequeueOutputBuffer(info, 10000)
        if (idx >= 0) {
            if (tracking) {
                memo.set(info.presentationTimeUs, output!![idx])
                memo.pos(0, output!![idx].limit())
                memo.id = info.flags
                next.scatter(memo)
                got = true
                output!![idx].clear()
                //Log.e("onFrameAvailable", "output=" + info.presentationTimeUs);
            }
            encoder!!.releaseOutputBuffer(idx, false)
        } else if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            tracking = true
            next.setFormat(0, SegmentFormat(encoder!!.outputFormat))
        }
        return got
    }

    internal fun flushOutput() {
        //encoder.signalEndOfInputStream();
        try {
            while (queryOutput()) {

            }
        } catch (e: Exception) {

        }

    }

    override fun release() {
        flushOutput()
        next.release()

        if (encoderSurface != null) {
            encoderSurface!!.release()
        }
        if (encoder != null) {
            encoder!!.stop()
            encoder!!.release()
        }
        if (env != null) {
            releaseGL()
        }
    }

    override fun seek(pts: Long, flag: Int) {

    }

    override fun addSink(sink: IMediaSink<NioSegment>, flag: Int) {
        this.next = sink
    }

    private fun force(format: SegmentFormat) {
        if (mUseWidthLimit && format.width > mWidthLimit) {
            val scale = format.width * 1f / mWidthLimit
            format.width = mWidthLimit
            format.width = (format.height / scale).toInt()
        } else if (mUseHeightLimit && format.height > mHeightLimit) {
            val scale = format.height * 1f / mHeightLimit
            format.height = mHeightLimit
            format.width = (format.width / scale).toInt()
        }
    }

    internal fun releaseGL() {
        env?.let {
            inputSurface?.release()
            render?.release(it)
            view?.release(it)

            out?.release()
            context?.release()
        }

    }

    internal fun createInputSurface(width: Int, height: Int) {
        env = GLEnv()

        context = GLContext(null, GLContext.FLAG_RECORDABLE)
        out = context!!.createWindowSurface(encoderSurface)
        out!!.makeCurrent()
        render = TextureRender()
        view = TextureDrawable(true, 0, 0)
        env?.let {
            render?.prepare(it)
            view?.prepare(it)
        }

        val texture = SurfaceTexture(view!!.texture!!.id)
        texture.setDefaultBufferSize(width, height)
        texture.setOnFrameAvailableListener(object : SurfaceTexture.OnFrameAvailableListener {
            internal val tr_texture = FloatArray(16)
            //final float[] tr_shape =new float[16];
            internal val tr_final = FloatArray(16)

            override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
                forceKeyFrame()
                texture.updateTexImage()
                val frame = ptsBuffer!!.read()
                val time = frame.pts * 1000
                //Log.e("onFrameAvailable", "Draw=" + frame.pts);
                texture.getTransformMatrix(tr_texture)

                Matrix.rotateM(tr_texture, 0, 90f, 0.5f, 0.5f, 0f);
                Matrix.multiplyMM(tr_final, 0, frame.tr_texture, 0, tr_texture, 0)
                view?.apply {
                    mtxShape.put(tr_final)
                    //todo!!!
                    draw(env!!,null as TextureRender)
                }
                out!!.setPresentationTime(time)
                out!!.swap()
                GLToolkit.checkError()
                waitNewFrame.open()
                try {
                    queryOutput()
                } catch (e: Exception) {

                }

            }
        })
        inputSurface = texture
        surfaceWaitor.open()
    }

    internal class Frame {
        var pts: Long = 0
        var tr_texture = FloatArray(16)
        var tr_shape = FloatArray(16)
    }

    internal class FrameBuffer(size: Int) {
        val frames = 0.rangeTo(size).map { Frame() }
        var read_pos: Int = 0
        var write_pos: Int = 0

        fun write(pts: Long, tr_2: FloatArray) {

            frames[write_pos].pts = pts
            System.arraycopy(tr_2, 0, frames[write_pos].tr_texture, 0, 16)
            System.arraycopy(tr_2, 16, frames[write_pos].tr_shape, 0, 16)
            write_pos = (write_pos + 1) % frames.size
        }

        fun read(): Frame {
            val frame = frames[read_pos]
            read_pos = (read_pos + 1) % frames.size
            return frame
        }
    }
}
