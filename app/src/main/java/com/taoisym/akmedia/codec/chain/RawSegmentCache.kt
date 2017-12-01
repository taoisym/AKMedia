package com.taoisym.akmedia.codec.chain

import com.taoisym.akmedia.codec.IMediaPoller
import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.RawSegment
import com.taoisym.akmedia.codec.SegmentFormat
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


class RawSegmentCache(cache: Int) : IMediaSink<RawSegment>, IMediaPoller<RawSegment> {
    override val format: SegmentFormat
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


    private var q: BlockingQueue<RawSegment>

    init {
        q = LinkedBlockingQueue(cache)
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    fun setState(state: Int, pts: Long) {

    }

    override fun prepare() {

    }

    override fun emit(data: RawSegment): Boolean {
        val push = RawSegment(data.meta)
        push.pos(data.offset, data.size)
        val alloc = ByteArray(data.size)
        System.arraycopy(data.buffer, 0, alloc, 0, data.size)
        push.set(data.pts, alloc)
        push.id = data.id
        try {
            q.put(push)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return true
    }

    override fun feedback(delay: Long) {

    }

    override fun release() {
        q.clear()
    }

    override fun <Front> setFront(poller: IMediaPoller<Front>) {

    }

    @Throws(InterruptedException::class)
    override fun pull(timeout: Long): RawSegment {
        try {
            return q.poll(timeout, TimeUnit.MICROSECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            throw e
        }

    }

    override fun seek(pts: Long, flag: Int) {
        q.clear()
    }
}
