package com.taoisym.akmedia.codec.avc

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import com.taoisym.akmedia.codec.*
import java.nio.ByteBuffer


class MediaEncoder(private var next: IMediaSink<NioSegment>) : IMediaSurfaceSink, IMediaSource<NioSegment, Unit> {
    internal var encoder: MediaCodec? = null
    internal var tracking: Boolean = false
    private var inputs: Array<ByteBuffer>? = null
    private var output: Array<ByteBuffer>? = null
    private val info = MediaCodec.BufferInfo()

    internal var param = Bundle()
    internal var memo = NioSegment(null)


    override fun prepare() {
        next.prepare()
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {

        if (encoder != null || ctx == null) {
            return null
        }
        val fmt = correctFormat(format)
        val mime = format.mime
        try {
            encoder = MediaCodec.createEncoderByType(mime)
            encoder!!.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder!!.start()
            inputs = encoder!!.inputBuffers
            output = encoder!!.outputBuffers

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    internal fun forceKeyFrame() {
        param.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
        encoder?.setParameters(param)
    }

    override fun emit(data: NioSegment): Boolean {
        val idx = encoder!!.dequeueInputBuffer(-1)
        if (idx >= 0) {
            //forceKeyFrame()
            inputs!![idx].put(data.buffer)
            encoder!!.queueInputBuffer(idx, 0, data.size, data.pts, 0)
            inputs!![idx].clear()
        }
        queryOutput()
        return false
    }

    private fun queryOutput(): Boolean {
        var got = false
        val idx = encoder!!.dequeueOutputBuffer(info, 10000)
        if (idx >= 0) {
            if (tracking) {

                memo.set(info.presentationTimeUs, output!![idx])
                memo.pos(0, output!![idx].limit())
                memo.id = info.flags
                next.emit(memo)
                got = true
                output!![idx].clear()
                //Log.e("onFrameAvailable", "o=" + info.size);
            }
            encoder!!.releaseOutputBuffer(idx, false)
        } else if (idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            tracking = true
            next.setFormat(0, SegmentFormat(encoder!!.outputFormat))
        }
        return got
    }

    override fun release() {
        while (queryOutput()) {

        }
        next.release()
        if (encoder != null) {
            encoder!!.stop()
            encoder!!.release()
        }
    }


    override fun seek(pts: Long, flag: Int) {

    }

    override fun addSink(sink: IMediaSink<NioSegment>, flag: Int) {
        this.next = sink
    }

    internal fun correctFormat(format: SegmentFormat): MediaFormat {
        var width = 0
        var height = 0
        var rate = 0
        if (format.isVideo) {
            if (width == 0 || height == 0 || rate == 0) {
                width = format.width
                height = format.height
                rate = 30
            }
            val fmt = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            //32 * width * height * frameRate / 100
            fmt.setInteger(MediaFormat.KEY_BIT_RATE, 32 * width * height * rate / 100)
            fmt.setInteger(MediaFormat.KEY_FRAME_RATE, rate)
            fmt.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            fmt.setInteger(MediaFormat.KEY_COLOR_FORMAT, format.colorFormat)
            return fmt
        } else {
            return format.format()
        }
    }

    private fun selectColorFormat(format: MediaFormat): Int {
        val mime = format.getString(MediaFormat.KEY_MIME)
        val codec = selectCodec(mime)
        return selectColorFormat(codec, mime)
    }

    private fun selectColorFormat(codecInfo: MediaCodecInfo?, mimeType: String): Int {
        val capabilities = codecInfo!!.getCapabilitiesForType(mimeType)
        for (i in capabilities.colorFormats.indices) {
            val colorFormat = capabilities.colorFormats[i]
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat
            }
        }
        return 0   // not reached
    }

    /**
     * Returns true if this is a color format that this camera code understands (i.e. we know how
     * to read and generate frames in this format).
     */
    private fun isRecognizedFormat(colorFormat: Int): Boolean {
        when (colorFormat) {
        // these are the formats we know how to handle for this camera
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar, MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> return true
            else -> return false
        }
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no
     * match was found.
     */
    private fun selectCodec(mimeType: String): MediaCodecInfo? {
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
            if (!codecInfo.isEncoder) {
                continue
            }
            val types = codecInfo.supportedTypes
            for (j in types.indices) {
                if (types[j].equals(mimeType, ignoreCase = true)) {
                    return codecInfo
                }
            }
        }
        return null
    }
}
