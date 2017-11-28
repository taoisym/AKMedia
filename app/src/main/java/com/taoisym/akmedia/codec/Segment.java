package com.taoisym.akmedia.codec;

import java.nio.ByteBuffer;

public class Segment<T> {
    public final SegmentFormat meta;
    public long pts;
    public int id;
    public int offset;
    public int size;
    public T buffer;

    public Segment(SegmentFormat meta) {
        this.meta = meta;
    }

    public ByteBuffer buffer() {
        return (ByteBuffer) buffer;
    }

    public T data() {
        return buffer;
    }

    public void pos(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    public void set(long pts, T buffer) {
        this.pts = pts;
        this.buffer = buffer;
    }

    public void reset() {
    }
}


