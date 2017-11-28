package com.taoisym.akmedia.codec.recoder

import android.graphics.SurfaceTexture
import android.media.MediaExtractor
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.codec.avc.MediaDecoder
import com.taoisym.akmedia.codec.avc.MediaMuxer
import com.taoisym.akmedia.codec.avc.MediaSource
import com.taoisym.akmedia.std.Lazy
import java.io.IOException
import java.util.*

class VideoRePresenter(input: List<MediaConvertor.VideoCropItem>, internal var output: String, internal var listener: MediaConvertor.ConvertListener?) : MediaConvertor(), IMediaSink<NioSegment> {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null


    private var mtx = FloatArray(32)//tr_texture and tr_shape
    private var input: MutableList<MediaConvertor.VideoCropItem>
    private var mainLooper: Handler
    private var sencder: ProgressSencder

    private var mCanceled: Boolean = false
    private var mWidthLimit: Int = 0
    private var mHeightLimit: Int = 0
    private var src: MediaSource? = null
    private lateinit var firstFormat: SegmentFormat

    private var next: Lazy<RedrawEncoder>? = null
    private lateinit var copier: MediaCopier
    private var outSurface: Lazy<SurfaceTexture>? = null
    private lateinit var current: MediaConvertor.VideoCropItem


    internal var lastAudioPts: Long = 0
    internal var segmentAuidoPts: Long = 0
    internal var lastVideoPts: Long = 0
    internal var segmentVideoPts: Long = 0

    init {
        this.input = Collections.synchronizedList(ArrayList(input))
        this.sencder = ProgressSencder()
        this.mainLooper = Handler(Looper.getMainLooper())
        Matrix.setIdentityM(mtx, 0)
    }

    fun setOutputWidthLimit(widthLimit: Int) {
        mWidthLimit = widthLimit
    }

    fun setOutputHeightLimit(heightLimit: Int) {
        mHeightLimit = heightLimit
    }

    override fun prepare() {
        thread = HandlerThread(this.javaClass.simpleName)
        thread?.start()
        handler = Handler(thread?.looper)
        handler?.post { next?.get()?.prepare() }
    }

    override fun start() {
        if (listener != null)
            mainLooper.post { listener!!.onStart() }

        prepare0()

        step_one()
    }

    private fun prepare0() {
        var muxer: MediaMuxer? = null
        try {
            muxer = MediaMuxer(output, 2)
            copier = AuidoCopier(input.size, muxer)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        val finalMuxer = muxer
        next = object : Lazy<RedrawEncoder>() {
            override fun refid(): RedrawEncoder {

                val writer = MediaCopier(finalMuxer)

                return RedrawEncoder(writer, handler!!, mWidthLimit, mHeightLimit)
            }
        }
        outSurface = object : Lazy<SurfaceTexture>() {
            override fun refid(): SurfaceTexture? {
                return next?.get()?.outputTexture
            }
        }
    }

    private fun step_one() {
        if (input.size > 0) {
            current = input.removeAt(0)
            val emitter = MediaSource(MediaSource.CONTINUE, MediaSource.STOP)
            src = emitter

            emitter.addSink(MediaDecoder(this, outSurface), 0)
            emitter.addSink(copier, 1)
            emitter.setSeekMode(MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            emitter.scatter(current.uri)
            emitter.setPlayRange(current.startPts, current.endPts)
            emitter.start()
        }
    }

    fun calacShapeMatix(a: SegmentFormat, b: SegmentFormat) {

        if (b.width.toFloat() / a.width > b.height.toFloat() / a.height) {
            val scale = b.height.toFloat() / a.height / (b.width.toFloat() / a.width)

            Matrix.scaleM(mtx, 16, 1f, scale, 1f)
        } else if (b.width.toFloat() / a.width < b.height.toFloat() / a.height) {
            val scale = b.width.toFloat() / a.width / (b.height.toFloat() / a.height)
            Matrix.scaleM(mtx, 16, scale, 1f, 1f)
        } else {
            //do nothing
        }
    }


    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        if (ctx == null) {
            Matrix.setIdentityM(mtx, 0)
            Matrix.setIdentityM(mtx, 16)
            val clone = changeFormat(format)
            if (firstFormat == null) {
                firstFormat = clone
            } else {
                calacShapeMatix(firstFormat, clone)
            }

            handler?.post { next?.get()?.setFormat(ctx, clone) }
        }
        return null
    }

    private fun changeFormat(from: SegmentFormat): SegmentFormat {

        var to = SegmentFormat(from)
        if (from.rotation == 90 || from.rotation == 270) {
            to.height = from.width
            to.width = from.height
        }
        if (from.rotation != 0) {
            Matrix.translateM(mtx, 0, 0.5f, 0.5f, 0f)
            Matrix.rotateM(mtx, 0, from.rotation.toFloat(), 0f, 0f, -1f)
            Matrix.translateM(mtx, 0, -0.5f, -0.5f, 0f)
        }
        return to
    }

    override fun scatter(o: NioSegment): Boolean {
        // TODO: 17-7-13 change pts here
        val memo = PresentSegment(o)
        memo.pts = segmentVideoPts + memo.pts - current.startPts
        lastVideoPts = memo.pts
        memo.buffer = mtx
        sencder.sendProgress(lastVideoPts)
        return next?.get()?.scatter(memo) ?: false
    }

    override fun release() {
        if (mCanceled) {
            handler?.post {
                next?.get()?.release()
                thread?.quit()
                if (listener != null) {
                    mainLooper.post { listener!!.onCancel() }
                }
            }
            return
        }

        segmentVideoPts = lastVideoPts
        if (input.size > 0) {
            handler?.postDelayed({ step_one() }, 1000)
        } else {
            handler?.post {
                next?.get()?.release()
                thread?.quit()
                if (listener != null) {
                    mainLooper.post { listener!!.onFinish(true) }
                }
            }
        }
    }

    fun cancel() {
        mCanceled = true
        if (src != null) {
            src!!.stop()
        }
    }


    override fun seek(pts: Long, flag: Int) {
        handler?.post { next?.get()?.seek(pts, flag) }
    }

    internal inner class AuidoCopier(var count: Int, muxer: MediaMuxer) : MediaCopier(muxer) {


        override fun scatter(nio: NioSegment): Boolean {
            nio.pts = segmentAuidoPts + nio.pts - (current?.startPts ?: 0)
            lastAudioPts = nio.pts
            return super.scatter(nio)
        }

        override fun release() {
            segmentAuidoPts = lastAudioPts
            count = count - 1
            if (count == 0)
                super.release()
        }
    }

    internal inner class ProgressSencder : Runnable {
        var lastPts: Long = 0

        fun sendProgress(pts: Long) {
            if (listener != null)
                if (pts - lastPts > 200000) {
                    lastPts = pts
                    mainLooper.post(this)
                }
        }

        override fun run() {
            listener!!.onProcess(lastPts)
        }
    }
}
