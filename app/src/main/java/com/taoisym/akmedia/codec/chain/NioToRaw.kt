package com.taoisym.akmedia.codec.chain

import android.media.MediaCodecInfo
import com.taoisym.akmedia.codec.*


class NioToRaw : IMediaSink<NioSegment>, IMediaSource<RawSegment, Unit> {

    private var next: IMediaSink<RawSegment>? = null

    private var buffer: ByteArray? = null

    constructor(next: IMediaSink<RawSegment>) {
        this.next = next
    }

    @Synchronized
    override fun scatter(data: NioSegment): Boolean {
        if (buffer == null || buffer!!.size < data.size) {
            buffer = ByteArray(data.size)
        }

        data.buffer.get(buffer)
        val meta = data.meta
        val memo = RawSegment(data.meta)
        memo.set(data.pts, buffer)
        memo.pos(0, data.size)
        //memo.id = id;
        val colorFormat = meta.colorFormat
        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar || colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
            //memo.buffer = InYUV.convertI420ToNV12(buffer, fileMeta.width, fileMeta.height);
        } else if (colorFormat == 0x7FA30C04) {

            //memo.buffer = InYUV.convertNV1232mToNV12(buffer, fileMeta.width, fileMeta.height);
            memo.pos(0, memo.buffer.size)
        }

        if (next != null)
            next!!.scatter(memo)
        if (meta.width > 0) {
            //Stats.print("frame pts="+data.pts);
        }
        return true
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        next!!.setFormat(ctx, format)
        return null
    }

    fun setState(state: Int, pts: Long) {

    }

    override fun prepare() {

    }

    override fun release() {
        next!!.release()
    }

    override fun seek(pts: Long, flag: Int) {
        next!!.seek(pts, flag)
    }

    override fun addSink(sink: IMediaSink<RawSegment>, flag: Int) {
        next = sink
    }


}
