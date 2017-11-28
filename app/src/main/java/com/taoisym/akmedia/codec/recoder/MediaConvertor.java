package com.taoisym.akmedia.codec.recoder;


public abstract class MediaConvertor {
    abstract public void start();

    public interface ConvertListener {
        void onStart();

        void onProcess(long pts);

        void onFinish(boolean success);

        void onCancel();
    }

    static public class VideoCropItem {
        public String uri;
        public long startPts = -1;//micro second
        public long endPts = -1;

        public VideoCropItem() {

        }

        public VideoCropItem(String uri, long startPts, long endPts) {
            this.uri = uri;
            this.startPts = startPts;
            this.endPts = endPts;
        }
    }
}
