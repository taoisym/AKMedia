package com.taoisym.akmedia.codec.audio;

import com.taoisym.akmedia.codec.avc.MediaDecoder;
import com.taoisym.akmedia.codec.avc.MediaSource;

public class MusicPlayer {

    AacPlayer pcm = new AacPlayer();
    private MediaSource src = new MediaSource(MediaSource.Companion.getPAUSE(), MediaSource.Companion.getCONTINUE());

    public void prepare(String file, boolean loop) {
        try {
            src.addSink(new MediaDecoder(pcm), 1);
            src.emit(file);
            src.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        src.start();
    }

    /**
     * @param volume should 0.0<=volume<=1.0
     */
    public void setVolume(float volume) {
        pcm.setVolume(volume);
    }


    public void seek(long pts) {
        src.seek(pts);
    }

    public void resume() {
        src.resume();
    }

    public void pause() {
        src.pause();
    }

    public void stop() {
        src.stop();
    }

    public void startOrResume(long seekTime) {
        int state = src.getState();
        if (state == MediaSource.Companion.getCONTINUE())
            return;
        else {
            src.resume();
        }
    }
}
