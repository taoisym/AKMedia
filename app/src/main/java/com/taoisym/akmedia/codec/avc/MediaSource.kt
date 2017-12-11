package com.taoisym.akmedia.codec.avc

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.ConditionVariable
import android.support.annotation.RequiresApi
import com.taoisym.akmedia.codec.*

import com.taoisym.akmedia.std.Stats
import java.util.concurrent.locks.ReentrantLock

/**
 *
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
class MediaSource(private val startState: Int, private var endState: Int) : IMediaSource<NioSegment, SurfaceTexture>, IMediaSink<String>, IMediaControl {


    protected lateinit var extractor: MediaExtractor
    protected lateinit var fastLookup: Array<IMediaSurfaceSink?>
    protected var crypto = MediaCodec.CryptoInfo()
    //volatile boolean loop = false;
    @Volatile
    var state = NotInit
    @Volatile protected var from: Long = 0
    @Volatile protected var to: Long = 0
    protected val stateLock = ReentrantLock()

    protected var fileMeta: AvcFileMeta? = null
    private var mediaPath: String? = null
    private var video: IMediaSurfaceSink? = null
    private var videoIdx = -1
    private var audioIdx = -1
    private var audio: IMediaSurfaceSink? = null
    @Volatile private var jumpPts: Long = -1
    private var l: IMediaStateListener? = null
    private var seekDir = MediaExtractor.SEEK_TO_NEXT_SYNC

    override val metaInfo: AvcFileMeta?
        get() = initMedia(false)
    internal var memo = NioSegment(null)


    override var rangeStart: Long
        get() = from
        set(start) = setRange(start, this.to)

    override var rangeEnd: Long
        get() = to
        set(end) = setRange(this.from, end)

    fun setSeekMode(seekDir: Int) {
        this.seekDir = seekDir
    }

    fun setStateChangeListener(l: IMediaStateListener) {
        this.l = l
    }

    override fun prepare() {

    }

    override fun start() {
        val waitor = ConditionVariable(false)
        Thread(object : Runnable {
            override fun run() {
                try {
                    prepare0()
                } catch (e: MediaException) {
                    e.printStackTrace()
                    waitor.open()
                    return
                }

                state = startState
                waitor.open()
                //finishWaitor.close();
                fixPrepareDecoderToDelay()
                loop()
                //finishWaitor.open();
            }

            private fun fixPrepareDecoderToDelay() {
                var fixDecoder = false
                if (Build.MODEL.toUpperCase().contains("X6D")) {
                    //mt6752 & Mali-T760
                    fixDecoder = true
                }

                if (fixDecoder) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
        }, "MediaSource").start()
        //waitor.block()
    }

    override fun stop() {
        state = STOP
        stateLock.unlock()
    }

    override fun pause() {
        if (state != CONTINUE)
            return
        state = PAUSE
        Stats.print("SetPAUSE:" + this)
    }

    override fun resume() {
        state = CONTINUE
        Stats.print("state=" + CONTINUE + this)
        stateLock.unlock()

        Stats.print("SetResume:" + this)
    }


    protected fun initMedia(force: Boolean): AvcFileMeta? {
        if (mediaPath == null) {
            return null
        }
        if (force || fileMeta == null) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(mediaPath)
            fileMeta = AvcFileMeta(retriever)
            fileMeta!!.path = mediaPath
            retriever.release()
            to = fileMeta!!.duration * 1000
        }
        return fileMeta
    }

    @Throws(MediaException::class)
    internal fun prepare0() {
        extractor = MediaExtractor()
        try {
            extractor.setDataSource(mediaPath!!)
            fastLookup = arrayOfNulls<IMediaSurfaceSink>(extractor.trackCount + 1)
            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mine = format.getString(MediaFormat.KEY_MIME)
                val type = if (mine.startsWith("mVideo/")) 0 else if (mine.startsWith("audio/")) 1 else -1
                if (type == -1)
                    continue

                var wrap: IMediaSurfaceSink? = null
                if (type == 0) {
                    wrap = video
                    video = null
                    videoIdx = i
                } else if (type == 1) {
                    wrap = audio
                    audio = null
                    audioIdx = i
                }
                if (wrap != null) {
                    wrap.prepare()
                    wrap.setFormat(extractor, SegmentFormat(format))
                    extractor!!.selectTrack(i)
                    fastLookup[i] = wrap
                }
            }
        } catch (e: Exception) {
            throw MediaException(e)
        }

    }

    private fun loop() {
        try {
            Stats.print("start codec:" + mediaPath + "state:" + state + this)
            l?.onStart()
            var advance = true// extractor.advance();
            var last = from
            jumpPts = from
            while (advance) {
                if (state == CONTINUE) {
                    if (jumpPts >= 0) {
                        if (videoIdx >= 0) {
                            seekToVideoFrame()
                        } else {
                            seekTo(jumpPts)
                            passSeekTo(jumpPts)
                        }
                        l?.onSeek(jumpPts)
                        jumpPts = -1
                    } else if (last <= to) {

                    } else {
                        jumpPts = from
                        last = from
                        state = this.endState
                        continue
                    }
                } else if (state == PAUSE) {
                    Stats.print("state=" + state + this)
                    if (jumpPts >= 0) {
                        if (videoIdx >= 0) {
                            seekToVideoFrame()
                        } else {
                            seekTo(jumpPts)
                            passSeekTo(jumpPts)
                        }
                        l?.onPause(last)
                        jumpPts = -1
                    } else {
                        l?.onPause(last)
                    }
                    Stats.print("PAUSE:" + this)
                    /**
                     * 状态可能被外界修改
                     */
                    if (state == PAUSE) {
                        stateLock.lock()
                        Stats.print("UnPAUSE:" + this)
                        if (state == PAUSE) {
                            if (jumpPts >= 0) {
                                if (videoIdx >= 0) {
                                    seekToVideoFrame()
                                } else {
                                    seekTo(jumpPts)
                                    passSeekTo(jumpPts)
                                }
                                jumpPts = -1
                            }
                            if (state == CONTINUE)
                                l?.onResume(last)
                            continue
                        } else if (state == CONTINUE) {
                            l?.onResume(last)
                        } else if (state == STOP) {
                            break
                        }
                    }
                } else if (state == STOP) {
                    break
                }

                val idx = extractor!!.sampleTrackIndex
                if (idx >= 0) {
                    val wrap = fastLookup!![idx]
                    if (wrap != null) {
                        Stats.print("Push " + this)

                        /**
                         * only save mVideo last pts value
                         */
                        val pts = extractor!!.sampleTime
                        memo.pts = pts
                        //Log.e("onFrameAvailable","type="+idx+" pts="+pts);
                        if (idx == videoIdx)
                            last = pts
                        wrap.emit(memo)
                    }
                    advance = extractor!!.advance()
                } else {
                    jumpPts = from
                    last = from
                    advance = true
                    state = this.endState
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Stats.print("exit")
        } finally {
            release()
            if (this.l != null) {
                l!!.onStop()
                //l=null;
            }
        }
    }

    private fun seekToVideoFrame() {
        if (videoIdx < 0)
            return
        //        if(audioIdx>=0)
        //            extractor.unselectTrack(audioIdx);
        val memo = NioSegment(null)
        try {
            var seek: Long
            do {
                seek = jumpPts
                seekTo(seek)
                fastLookup[videoIdx]?.seek(seek, 1)
                var got = false
                while (!got) {
                    val idx = extractor!!.sampleTrackIndex
                    if (idx < 0)
                        return
                    /**
                     * 停止时还在seek,直接跳出
                     */
                    if (state == STOP)
                        return
                    //                    if(seek!=jumpPts)
                    //                        break;
                    if (idx == videoIdx) {
                        val wrap = fastLookup[videoIdx]
                        got = wrap!!.emit(memo)
                    }
                    if (extractor!!.advance() == false) {
                        return
                    }
                }
            } while (seek != jumpPts)
            //Log.e("Last","Last seek pts="+seek);
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fastLookup[videoIdx]?.seek(jumpPts, 2)
        }
        Stats.printNow("seek begin")
    }

    private fun seekTo(pts: Long) {
        extractor!!.seekTo(pts, seekDir)
        Stats.printNow("seeking")
    }

    private fun passSeekTo(pts: Long) {
        for (decoder in fastLookup!!) {
            decoder?.seek(pts, 0)
        }
    }

    /**
     * this fun only seek,will not goto next mVideo frame,now used for audio play
     *
     * @param pts
     * @param flag
     */
    override fun seek(pts: Long, flag: Int) {
        //        if(extractor!=null&&flag==1)
        //            extractor.seekTo(pts, MediaExtractor.SEEK_TO_NEXT_SYNC);
    }

    override fun seek(pts: Long) {
        if (pts >= from && pts <= to) {
            jumpPts = pts
            if (state == PAUSE) {
                stateLock.unlock()
            }
        }
    }

    override fun addSink(sink: IMediaSurfaceSink, flag: Int) {
        assert(flag >= 0 && flag <= 2)
        if (flag == 0) {
            video = sink
        } else if (flag == 1) {
            audio = sink
        }
    }

    override fun release() {
        state = STOP
        if (fastLookup != null) {
            for (decoder in fastLookup) {
                decoder?.release()
            }
            fastLookup.fill(null)
        }
        extractor?.release()
        Stats.print("Release codec:" + mediaPath!!)
    }

    override fun emit(file: String): Boolean {
        mediaPath = file
        return initMedia(true) != null
    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }


    override fun setPlayRange(from: Long, to: Long) {
        setRange(from, to)
    }

    internal fun setRange(from: Long, to: Long) {
        var from = from
        var to = to
        if (to < 0) {
            to = this.to
        }
        if (from < 0) {
            from = this.from
        }
        if (from > to) {
            val swap = from
            this.from = to
            this.to = swap
        } else {
            this.from = from
            this.to = to
        }
    }

    class MediaException(internal var e: Exception) : Exception() {

        init {
            e.printStackTrace()
        }
    }

    companion object {
        val NotInit = -1
        val CONTINUE = 0
        val PAUSE = 1
        val STOP = 2
    }
}
