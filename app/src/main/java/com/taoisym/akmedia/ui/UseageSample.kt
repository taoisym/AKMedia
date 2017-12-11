package com.taoisym.akmedia.ui

import Filters
import android.content.Context
import android.graphics.SurfaceTexture
import com.taoisym.akmedia.camera.AkCamera
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.codec.VideoDir
import com.taoisym.akmedia.codec.audio.AacPlayer
import com.taoisym.akmedia.codec.audio.MusicPlayer
import com.taoisym.akmedia.codec.avc.MediaDecoder
import com.taoisym.akmedia.codec.avc.MediaMuxer
import com.taoisym.akmedia.codec.avc.MediaSource
import com.taoisym.akmedia.codec.avc.MediaWriter
import com.taoisym.akmedia.render.GLEnv
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.std.Lazy
import com.taoisym.akmedia.std.Supplier
import com.taoisym.akmedia.video.FileTarget
import com.taoisym.akmedia.video.RealSurface
import com.taoisym.akmedia.video.SurfaceTarget


class UseageSample {
    var vg: VideoSenceSample? = null
    private var mp4: FileTarget? = null

    fun camera(camera: AkCamera, main: Supplier<RealSurface>) {
        val context = GLEnv()
        vg = VideoSenceSample(SurfaceTarget(main))
        val size = camera.parameter.previewSize
        val fmt = SegmentFormat(size.width, size.height, 0)
        fmt.dir=camera.face?.let {
            if(it===true) VideoDir.FLIP_XY else VideoDir.FLIP_X
        }?:VideoDir.FLIP_Y
        fmt.rotation=90
        vg?.prepare()
        vg?.setFormat(context, fmt)
        camera.setPreviewTexture(vg!!.target)
        camera.startPreview()
    }
    fun video(uri:String,main: Supplier<RealSurface>){
        val context = GLEnv()
        val format=SegmentFormat(uri)
        vg = VideoSenceSample(SurfaceTarget(main))
        val fmt = SegmentFormat(format.width, format.height, 0)
        fmt.dir=VideoDir.FLIP_Y
        vg?.prepare()
        vg?.setFormat(context, fmt)

        val video = MediaSource(MediaSource.CONTINUE, MediaSource.CONTINUE)
        video.addSink(MediaDecoder(vg!!), 0)
        video.addSink(MediaDecoder(AacPlayer()), 1)
        video.emit(uri)
        video.start()
    }

    fun start(ctx: Context) {
        if (mp4 != null)
            return
        val muxer = MediaMuxer("/sdcard/cam.mp4", 1)
        val writer = MediaWriter(muxer)
        mp4 = FileTarget(writer)
        mp4?.let {
            vg?.addSink(it, 1)
            vg?.add(ctx)
        }


    }

    fun stop() {
        mp4?.let {
            vg?.delSink(it)
            vg?.del()

        }

        mp4 = null
    }
    fun release(){
        vg?.release()
    }
    companion object {
        var filter=0
    }
    fun change(ctx: Context) {
        if(filter>= Filters.size){
            filter=0
        }
        var render:TextureRender= Filters.get(ctx,filter)
        vg?.setFilter(render)
        ++filter
    }
}
