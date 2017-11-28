package com.taoisym.akmedia.codec;

import android.media.MediaMetadataRetriever;


public class AvcFileMeta {
    public final int rotate;
    public final long duration;
    public final boolean hasVideo;
    public final boolean hasAudio;
    public final int width;
    public final int height;
    public String path;


    public AvcFileMeta(MediaMetadataRetriever retriever) {
        rotate = intValue(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        duration = longValue(retriever, MediaMetadataRetriever.METADATA_KEY_DURATION);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            framerate = intValue(retriever, MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
//        }
        hasVideo = boolValue(retriever, MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
        hasAudio = boolValue(retriever, MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
        width = intValue(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        height = intValue(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        retriever.release();
    }

    long longValue(MediaMetadataRetriever retriever, int key) {
        try {
            String value = retriever.extractMetadata(key);
            return Long.valueOf(value);
        } catch (Exception e) {
            return 0;
        }
    }

    int intValue(MediaMetadataRetriever retriever, int key) {
        try {
            String value = retriever.extractMetadata(key);
            return Integer.valueOf(value);
        } catch (Exception e) {
            return 0;
        }
    }

    boolean boolValue(MediaMetadataRetriever retriever, int key) {
        String value = retriever.extractMetadata(key);
        return "yes".equals(value);
    }
}
