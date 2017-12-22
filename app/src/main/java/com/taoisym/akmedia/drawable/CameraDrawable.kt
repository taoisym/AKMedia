package com.taoisym.akmedia.drawable

import com.taoisym.akmedia.camera.AkCamera
import com.taoisym.akmedia.codec.SegmentFormat
import com.taoisym.akmedia.render.GLEnv

class CameraDrawable(val camera: AkCamera) : ExternalDrawable(0, 0), PlayAble {
    override fun prepare() {

    }

    override fun setFormat(ctx: Any, format: SegmentFormat): Any? {
        return null
    }

    override fun release() {
    }

    override fun prepare(env: GLEnv) {
        super.prepare(env)
    }


    override fun start() {
        camera.open(false){
            success ->
            if (success) {
                camera.applyParameter()
                camera.setPreviewTexture(target)
            }
        }
    }


    override fun stop() {

    }

    override fun pause() {
    }

    override fun resume() {
    }
}