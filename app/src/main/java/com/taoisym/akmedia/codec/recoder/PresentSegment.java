package com.taoisym.akmedia.codec.recoder;

import com.taoisym.akmedia.codec.NioSegment;
import com.taoisym.akmedia.codec.Segment;


public class PresentSegment extends Segment<float[]> {
    public PresentSegment(NioSegment nio) {
        super(nio.meta);
        this.pts = nio.pts;
    }
}