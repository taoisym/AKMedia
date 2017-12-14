package com.taoisym.akmedia.codec.audio

import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.taoisym.akmedia.codec.*

import com.taoisym.akmedia.codec.chain.ByteRingBuffer

import java.util.concurrent.locks.ReentrantLock


class PcmMixer : IMediaSink<NioSegment>, IMediaSource<RawSegment, Unit> {

    protected var first = ByteRingBuffer(SIZE)
    protected var second = ByteRingBuffer(SIZE)

    protected var next: IMediaSink<RawSegment>? = null

    protected var mix0 = ByteArray(SIZE)
    protected var mix1 = ByteArray(SIZE)
    protected val mixLock = ReentrantLock()

    @Synchronized
    override fun emit(data: NioSegment): Boolean {
        if (data == null) {
            //next.emit(null);
            return true
        }
        val ref = data.buffer
        val size = data.size
        var select: ByteRingBuffer? = null
        if (data.id == 0) {
            select = first
        } else {
            select = second
        }
        while (select.free < data.size) {
            mixLock.lock()


        }
        ref.get(mix0, data.offset, size)
        select.write(mix0, 0, size)
        //mix and output
        val min = Math.min(first.used, second.used)
        if (min == 0) {
            return true
        }
        if (min > 0) {
            val memo = RawSegment(data.meta)
            memo.pos(0, min)
            memo.set(data.pts, mix(min))
            next!!.emit(memo)
            mixLock.unlock()
        } else {
            if (first.used > 0) {
                select = first
            } else if (second.used > 0) {
                select = second
            } else {
                return true
            }
            val used = select.used
            select.read(mix0, 0, used)
            //memo.set(data.pts,mix0);
            //memo.pos(0,used);
            //next.emit(memo);
        }
        return true
    }

    protected fun mix(size: Int): ByteArray {
        first.read(mix0, 0, size)
        second.read(mix1, 0, size)
        for (i in 0 until size) {
            val samplef1 = mix0[i] / 128.0f
            val samplef2 = mix1[i] / 128.0f
            var mixed = samplef1 + samplef2
            // reduce the volume a bit:
            mixed *= 0.8f
            // hard clipping
            if (mixed > 1.0f) mixed = 1.0f
            if (mixed < -1.0f) mixed = -1.0f
            val outputSample = (mixed * 128.0f).toByte()
            mix0[i] = outputSample
        }
        return mix0
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        next!!.setFormat(ctx, format)
        Log.e("format", format.toString())
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

    }

    override fun addSink(sink: IMediaSink<RawSegment>, flag: Int) {
        next = sink
    }

    companion object {
        internal val SIZE = 20 * 1024
    }


}
