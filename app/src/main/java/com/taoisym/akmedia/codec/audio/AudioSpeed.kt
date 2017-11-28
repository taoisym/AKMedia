package com.taoisym.akmedia.codec.audio

import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.IMediaSource
import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.codec.chain.ByteRingBuffer
import java.nio.ByteBuffer


class AudioSpeed(internal var next: IMediaSink<NioSegment>, private val speed: Float) : IMediaSource<NioSegment, Unit>, IMediaSink<NioSegment> {
    private lateinit var memo: NioSegment

    private lateinit var ring: ByteRingBuffer
    private lateinit var pipe: ByteBuffer
    private var outputReqestSize: Int = 0
    private lateinit var sampleBuffer: ByteArray


    override fun seek(pts: Long, flag: Int) {

    }

    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        next.setFormat(ctx, format)
        return null
    }

    override fun scatter(data: NioSegment): Boolean {
        if (speed.toDouble() == 1.0) {
            return next.scatter(data)
        }
        if (memo == null) {
            memo = NioSegment(data.meta)
            val factor = if (speed > 1.0f) speed else 1 / speed
            outputReqestSize = (Math.floor((data.size * factor / 2).toDouble()) * 2).toInt()
            pipe = ByteBuffer.allocate(outputReqestSize)
            ring = ByteRingBuffer(outputReqestSize + data.size)
            sampleBuffer = ByteArray(outputReqestSize * 4)
            memo!!.buffer = pipe
        }


        if (speed < 1.0) {//read data size ,output > data.size
            data.buffer.get(sampleBuffer, 0, data.size)
            var i = outputReqestSize - 2
            while (i > -0) {
                val idx = (2 * Math.floor((i * speed / 2).toDouble())).toInt()
                sampleBuffer[i] = sampleBuffer!![idx]
                sampleBuffer[i + 1] = sampleBuffer!![idx + 1]
                i = i - 2
            }
            pipe.clear()
            pipe.put(sampleBuffer)
            pipe.flip()
            memo.pts = data.pts
            memo.size = outputReqestSize
            next.scatter(memo)
        } else {//read request size,output /speed size
            data.buffer.get(sampleBuffer, 0, data.size)
            ring.write(sampleBuffer, 0, data.size)
            val remain = ring.used
            if (remain >= outputReqestSize) {
                ring.read(sampleBuffer, 0, remain)
                val loop = Math.floor((remain / speed).toDouble()).toInt() / 2
                var i = 0
                while (i < loop) {
                    val idx = (2 * Math.floor((i * speed / 2).toDouble())).toInt()
                    sampleBuffer[i] = sampleBuffer!![idx]
                    sampleBuffer[i + 1] = sampleBuffer!![idx + 1]
                    i = i + 2
                }
                pipe.clear()
                pipe.put(sampleBuffer, 0, loop)
                pipe.flip()
                memo.size = loop
                next.scatter(memo)
            } else {
                memo.pts = data.pts
            }
        }
        return true
    }

    override fun addSink(next: IMediaSink<NioSegment>, flag: Int) {
        this.next = next
    }

    override fun release() {
        next.release()
    }
}
