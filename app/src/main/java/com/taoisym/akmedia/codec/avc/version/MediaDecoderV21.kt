package com.taoisym.akmedia.codec.avc.version

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import com.taoisym.akmedia.codec.*

/**
 * set format must run in handle thread,
 * because callback in current thread
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MediaDecoderV21(val next:IMediaSurfaceSink):IMediaSurfaceSink, IMediaSource<NioSegment, Unit>{
    internal var codec: MediaCodec? = null
    override fun seek(pts: Long, flag: Int) {
        next.seek(pts,flag)
    }

    override fun addSink(pass: IMediaTarget<NioSegment, Unit>, flag: Int) {
    }

    override fun prepare() {
        next.prepare()
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        codec= MediaCodec.createByCodecName(format.mime)
        codec?.run {
            var texture:SurfaceTexture?=null
            try {
                texture=next.target.get()
            }catch (e:NotImplementedError){

            }
            val fmt=format.format()
            fmt.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            configure(fmt, Surface(texture),null,0)
            setCallback(CodecCallback())
            start()
        }
        return null
    }

    override fun emit(data: NioSegment): Boolean {
        return true
    }

    override fun release() {
        codec?.run {
            release()
        }
        next.release()
    }
    inner class CodecCallback():MediaCodec.Callback(){
        var memo=NioSegment(null)
        override fun onOutputBufferAvailable(codec: MediaCodec?, index: Int, info: MediaCodec.BufferInfo?) {
            val buf=codec?.getOutputBuffer(index)
            memo.set(info?.presentationTimeUs?:0,buf)
            memo.pos(0,buf?.remaining()?:0)
            next.emit(memo)
        }

        override fun onInputBufferAvailable(codec: MediaCodec?, index: Int) {
        }

        override fun onOutputFormatChanged(codec: MediaCodec?, format: MediaFormat?) {
            memo=NioSegment(SegmentFormat(format))
        }

        override fun onError(codec: MediaCodec?, e: MediaCodec.CodecException?) {
        }
    }

}
