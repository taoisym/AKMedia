package com.taoisym.akmedia.codec;

import android.media.MediaFormat;


public class SegmentFormat {
    public final String mime;
    public final int channel;
    public final int sample;
    public int width;
    public int height;
    public int rotation;
    public int colorFormat;
    public int MAX_INPUT_SIZE;
    public int BIT_RATE;
    public int FRAME_RATE = 30;
    public int I_FRAME_INTERVAL;
    MediaFormat format;

    public SegmentFormat(SegmentFormat format) {
        this.mime = format.mime;
        this.channel = format.channel;
        this.sample = format.sample;
        this.width = format.width;
        this.height = format.height;
        this.colorFormat = format.colorFormat;
        this.rotation = format.rotation;
        this.MAX_INPUT_SIZE = format.MAX_INPUT_SIZE;
    }

    public SegmentFormat(MediaFormat format) {
        this.format = format;
        mime = format.getString(MediaFormat.KEY_MIME);
        width = format.getInteger(MediaFormat.KEY_WIDTH);
        height = format.getInteger(MediaFormat.KEY_HEIGHT);
        try {
            colorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
        } catch (Exception e) {
        }
        try {
            rotation = format.getInteger(MediaFormat.KEY_ROTATION);
        } catch (Exception e) {

        }
        try {
            MAX_INPUT_SIZE = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } catch (Exception e) {

        }
        this.channel = 0;
        this.sample = 0;
    }

    public SegmentFormat(int width, int height, int colorFormat) {
        this.mime = MediaFormat.MIMETYPE_VIDEO_AVC;
        this.width = width;
        this.height = height;
        this.colorFormat = colorFormat;
        this.channel = 0;
        this.sample = 0;
        this.MAX_INPUT_SIZE = 0;
    }

    public SegmentFormat(int sampleRate, int channel) {
        this.mime = MediaFormat.MIMETYPE_AUDIO_AAC;
        this.channel = channel;
        this.sample = sampleRate;
        this.width = 0;
        this.height = 0;
        this.colorFormat = 0;
        this.MAX_INPUT_SIZE = 0;
    }

    public MediaFormat format() {
        if (format != null) {
            return format;
        }
        MediaFormat fmt = mime.equals(MediaFormat.MIMETYPE_VIDEO_AVC) ?
                MediaFormat.createVideoFormat(mime, width, height) :
                MediaFormat.createAudioFormat(mime, sample, channel);
        fmt.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);
        fmt.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        fmt.setInteger(MediaFormat.KEY_ROTATION, rotation);
        fmt.setInteger(MediaFormat.KEY_BIT_RATE, 32 * width * height * FRAME_RATE / 100);
        fmt.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        fmt.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        return fmt;
    }

    public SegmentFormat align() {
        width = width << 4 >> 4;
        height = height << 4 >> 4;
//        format.setInteger(MediaFormat.KEY_BIT_RATE, 32 * width * height * FRAME_RATE / 100);
//        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
//        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        return this;
    }

    public boolean isVideo() {
        return mime.equals(MediaFormat.MIMETYPE_VIDEO_AVC);
    }
}
