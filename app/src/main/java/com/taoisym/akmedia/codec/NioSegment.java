package com.taoisym.akmedia.codec;


import java.nio.ByteBuffer;

public class NioSegment extends Segment<ByteBuffer> {
    public NioSegment(SegmentFormat meta) {
        super(meta);
    }

    @Override
    public void reset() {
        buffer.flip();
    }
}


