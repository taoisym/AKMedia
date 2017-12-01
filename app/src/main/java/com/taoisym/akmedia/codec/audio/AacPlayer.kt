package com.taoisym.akmedia.codec.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import com.taoisym.akmedia.codec.IMediaSink
import com.taoisym.akmedia.codec.NioSegment
import com.taoisym.akmedia.codec.SegmentFormat
import java.nio.ByteBuffer
import java.util.*



class AacPlayer : IMediaSink<NioSegment> {

    protected var track: AudioTrack? = null
    protected var mute: Boolean = false
    //two buffer for mute
    protected lateinit var muteBuffer: ByteBuffer
    protected lateinit var buffer: ByteArray


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            muteBuffer = ByteBuffer.allocate(4096 * 2)
        } else {
            buffer = ByteArray(4096 * 2)
        }
    }

    var volume: Float = 1f
        set(value) {
            field = value
            if (track != null)
                track!!.setStereoVolume(value, value)
        }

    @Synchronized
    fun mute(mute: Boolean) {
        if (mute) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val temp = ByteArray(BUFFER_LENGTH)
                muteBuffer.clear()
                muteBuffer.put(temp)
            } else {
                Arrays.fill(buffer, 0x0.toByte())
            }
        }
        this.mute = mute
    }

    @Synchronized
    override fun emit(data: NioSegment): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mute) {
                muteBuffer.clear()
                track!!.write(muteBuffer, data.size, AudioTrack.WRITE_BLOCKING)
            } else
                track!!.write(data.buffer, data.size, AudioTrack.WRITE_BLOCKING)
        } else {
            if (mute) {
                track!!.write(buffer, data.offset, data.size)
            } else {
                data.buffer.get(buffer)
                track!!.write(buffer, data.offset, data.size)
            }
        }
        return true
    }

    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        if (ctx == null) {
            return null
        }
        //int streamType, int sampleRateInHz, int channelConfig, int audioFormat,
        //int bufferSizeInBytes, int mode
        //mime = format.getString(MediaFormat.KEY_MIME);

        val out = if (format.channel == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO

        //out=AudioFormat.CHANNEL_OUT_MONO;
        val enc = AudioFormat.ENCODING_PCM_16BIT//format.getInteger(MediaFormat.KEY_PCM_ENCODING);


        // if duration is 0, we are probably playing a live stream
        //long duration = format.getLong(MediaFormat.KEY_DURATION);
        //int  bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);

        val bufsize = AudioTrack.getMinBufferSize(format.sample, out,
                enc) * 4
        //        Log.e("audio:", bufsize + "bytes buffer");

        track = AudioTrack(AudioManager.STREAM_MUSIC, format.sample, out,
                enc, bufsize,
                AudioTrack.MODE_STREAM)
        volume = volume
        track!!.play()
        return null
    }

    fun setState(state: Int, pts: Long) {

    }

    override fun prepare() {

    }

    override fun release() {
        if (track != null) {
            track!!.stop()
            track!!.release()
            track = null
        }
    }


    override fun seek(pts: Long, flag: Int) {

    }

    companion object {
        internal val BUFFER_LENGTH = 4096 * 2
    }
}
