package com.taoisym.akmedia.camera.v2

import android.graphics.SurfaceTexture
import com.taoisym.akmedia.camera.*

import com.taoisym.akmedia.std.Reformer
import com.taoisym.akmedia.std.Supplier


class AkCameraV2 : AkCamera {
    override var face: Boolean?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override val parameter: AkCamera.Parameter
        get() = throw RuntimeException()

    override fun open(id: Boolean, cameraOpenCallback: CameraOpenCallback) {

    }

    override fun close() {

    }

    override fun release() {

    }

    override fun setPreviewCallback(callback: PreviewCallback) {

    }

    override fun setPreviewTexture(textureSupplier: Supplier<SurfaceTexture>) {

    }


    override fun startPreview() {

    }

    override fun stopPreview() {

    }

    override fun takePhoto(focus: Boolean, callback: TakePhotoCallback) {

    }

    override fun setFocusArea(supplier: Reformer<List<AkCamera.Area>>) {

    }

    override fun setMeterArea(supplier: Reformer<List<AkCamera.Area>>) {

    }

    override fun setParameterReformer(reformer: Reformer<AkCamera.Parameter>, fineControl: Reformer<AkCamera.Parameter>) {

    }

    override fun focus(callback: FocusCallback) {

    }

    override fun applyParameter() {

    }

    override fun meter(auto: Boolean) {

    }

}
