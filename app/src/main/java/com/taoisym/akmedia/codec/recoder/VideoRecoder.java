package com.taoisym.akmedia.codec.recoder;

import android.media.MediaExtractor;
import android.os.Handler;
import android.os.Looper;

import com.taoisym.akmedia.codec.NioSegment;
import com.taoisym.akmedia.codec.avc.MediaDecoder;
import com.taoisym.akmedia.codec.avc.MediaEncoder;
import com.taoisym.akmedia.codec.avc.MediaMuxer;
import com.taoisym.akmedia.codec.avc.MediaSource;
import com.taoisym.akmedia.codec.chain.Passer;

import java.io.IOException;


public class VideoRecoder extends MediaConvertor {
    String in;
    VideoCropItem crop;
    String out;
    ConvertListener listener;
    Handler uiThreaded = new Handler(Looper.getMainLooper());
    Sender sender = new Sender();

    public VideoRecoder(String in, String out, ConvertListener listener) {
        this.in = in;
        this.out = out;
        this.listener = listener;
    }

    public VideoRecoder(VideoCropItem crop, String out, ConvertListener listener) {
        this.crop = crop;
        this.out = out;
        this.listener = listener;
    }

    @Override
    public void start() {
        MediaMuxer muxer = null;
        try {
            muxer = new MediaMuxer(out, 2);
        } catch (IOException e) {
            e.printStackTrace();
            sendFinish(false);
            return;
        }


        MediaDecoder decoder = new MediaDecoder(new MediaEncoder(new MediaCopier(muxer)));
        /**
         * hook
         */
        Passer passer = new Passer<NioSegment>(decoder) {
            @Override
            public void prepare() {
                super.prepare();
                sendStart();
            }

            @Override
            public boolean scatter(NioSegment o) {
                sendProcess(o.pts);
                return super.scatter(o);
            }

            @Override
            public void release() {
                super.release();
                sendFinish(true);
            }


        };
        MediaSource src = new MediaSource(MediaSource.Companion.getCONTINUE(), MediaSource.Companion.getSTOP());
        src.addSink(new SegmentReader(new MediaCopier(muxer)), 1);
        src.addSink(passer, 0);
        if (in != null) {
            src.scatter(in);
        } else if (crop != null) {
            src.setSeekMode(MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            src.setPlayRange(crop.startPts, crop.endPts);
            src.scatter(crop.uri);
        } else {

            return;
        }
        src.start();
    }

    void sendStart() {
        if (listener != null) {
            uiThreaded.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStart();
                }
            });
        }
    }

    void sendProcess(long pts) {
        sender.send(pts);
    }

    void sendFinish(final boolean success) {
        if (listener != null) {
            uiThreaded.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFinish(success);
                }
            });
        }
    }

    class Sender implements Runnable {
        long lastSend;

        void send(long pts) {
            //0.1 second
            if (pts - lastSend > 100000) {
                lastSend = pts;
                uiThreaded.post(this);
            }
        }

        @Override
        public void run() {
            listener.onProcess(lastSend);
        }
    }
}
