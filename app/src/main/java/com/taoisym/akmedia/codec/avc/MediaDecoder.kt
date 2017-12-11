package com.taoisym.akmedia.codec.avc

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import com.taoisym.akmedia.codec.*
import com.taoisym.akmedia.std.Lazy
import com.taoisym.akmedia.std.Stats
import java.nio.ByteBuffer
import java.nio.ByteOrder


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
class MediaDecoder @JvmOverloads constructor(protected val next: IMediaSurfaceSink) : IMediaSurfaceSink, IMediaSource<NioSegment, SurfaceTexture> {
    private var output: Surface? = null
    private var present: Boolean=false
    private lateinit var extractor: MediaExtractor
    private var decoder: MediaCodec? = null
    private var info = MediaCodec.BufferInfo()
    private var inputs: Array<ByteBuffer>? = null
    private var outputs: Array<ByteBuffer>? = null
    private var mime: String? = null
    private val id: Int = 0
    internal lateinit var meta: SegmentFormat
    private var seeking: Boolean = false
    private var jumpTo: Long = 0
    private var decoderWaitTime = DEFAULT_VIDEO_DECODE_WAIT_TIME


    override fun emit(data: NioSegment): Boolean {
        var ret = false
        val stats = Stats()
        var state = decoder!!.dequeueInputBuffer(-1)
        if (state >= 0) {
            val bf = inputs!![state]
            bf.clear()
            bf.order(ByteOrder.nativeOrder())
            var size = extractor.readSampleData(bf, 0)
            val pts = extractor.sampleTime
            val flag = extractor.sampleFlags
            if (size < 0 || size <= 8) {//8 is magic code
                size = 0
            }
            decoder!!.queueInputBuffer(state, 0, size, pts, flag)
            bf.clear()
        } else {
            Stats.print("Drop")
        }

        do {
            state = decoder!!.dequeueOutputBuffer(info, decoderWaitTime.toLong())

            if (state >= 0) {
                val out = outputs!![state]
                try {
                    if (info.size > 0) {
                        val memo = NioSegment(meta)
                        memo.pos(info.offset, info.size)
                        memo.set(info.presentationTimeUs, out)
                        //Stats.print("vout pts="+info.presentationTimeUs);
                        if (seeking) {
                            if (memo.pts >= jumpTo) {
                                ret = true
                                next?.emit(memo)
                                seeking = false
                                return true
                            }
                        } else {
                            ret = true
                            next?.emit(memo)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    release()
                } finally {
                    out?.clear()
                    //Log.e("onFrameAvailable","Preset="+info.presentationTimeUs);
                    if (decoder != null)
                        decoder!!.releaseOutputBuffer(state, if (seeking) false else present)
                }
            } else if (state == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputs = decoder!!.outputBuffers
            } else if (state == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                setNextSinkFormat(id, SegmentFormat(decoder!!.outputFormat))
            } else if (state == MediaCodec.INFO_TRY_AGAIN_LATER) {
            }
        } while (state != MediaCodec.INFO_TRY_AGAIN_LATER)
        stats.printUsedTime("decode/consume fragment")
        return ret
    }

    override fun release() {
        try {
            next?.apply {
                release()
            }

            decoder?.apply {
                stop()
                release()
            }
            output?.release()
        } catch (e: Exception) {

        }

    }

    internal fun setNextSinkFormat(id: Int, format: SegmentFormat) {
        if (mime!!.startsWith("audio/")) {
            decoderWaitTime = 3000
            meta = SegmentFormat(format)
        } else {
            decoderWaitTime = DEFAULT_VIDEO_DECODE_WAIT_TIME
            meta = SegmentFormat(format)
        }
        next?.setFormat(id, format)
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        if (ctx == null)
            return null
        this.extractor = ctx as MediaExtractor
        next?.setFormat(Unit, format)
        mime = format.mime
        //关闭默认的旋转，绘制时控制旋转

        val fmt = format.format()
        fmt.setInteger(MediaFormat.KEY_ROTATION, 0)
        fmt.setInteger("rotation", 0)

        try {
            val decoder = MediaCodec.createDecoderByType(mime)
            this.decoder = decoder
            try{
                //no target should throw exception
                output = Surface(next.target.get())
                this.present = true
            }catch (e:NotImplementedError){

            }
            decoder.configure(fmt, output, null, 0 /* Decoder */)
            decoder.start()
            inputs = decoder.inputBuffers
            outputs = decoder.outputBuffers
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    override fun prepare() {
        next?.prepare()
    }

    protected fun setSeekingState(flag: Int, jumpPts: Long) {
        seeking = flag == 1
        this.jumpTo = jumpPts
    }

    override fun seek(pts: Long, flag: Int) {
        if (flag == 0) {
            decoder!!.flush()
            next?.seek(pts, flag)
        } else if (flag == 1) {
            decoder!!.flush()
            setSeekingState(flag, pts)
        } else {
            setSeekingState(flag, pts)
        }
    }

    override fun addSink(sink: IMediaSurfaceSink, flag: Int) {
//        next = sink
    }

    companion object {
        private val DEFAULT_VIDEO_DECODE_WAIT_TIME = 10000
    }

}
