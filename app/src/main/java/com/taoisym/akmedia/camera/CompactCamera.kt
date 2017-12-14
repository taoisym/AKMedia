package com.taoisym.akmedia.camera

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread

import com.taoisym.akmedia.camera.v1.AkCameraV1
import com.taoisym.akmedia.std.Reformer
import com.taoisym.akmedia.std.Supplier


class CompactCamera : AkCamera {
    override var face: Boolean?
        get() = mImpl.face
        set(value) {
            mImpl.face=value
        }
    protected lateinit var mImpl: AkCamera
    protected var mCameraThread: HandlerThread? = null
    protected var mPostor: Handler

    override val parameter: AkCamera.Parameter
        get() {
            return mImpl.parameter
//            val parameter = Supplier<AkCamera.Parameter>()
//            mPostor.post {
//                parameter.set(mImpl.parameter)
//            }
//            return parameter.get()
        }

    init {
        mCameraThread = object : HandlerThread("CompactCamera") {
            override fun onLooperPrepared() {
                super.onLooperPrepared()
                mImpl = AkCameraV1()
            }
        }
        mCameraThread!!.start()
        mPostor = Handler(mCameraThread!!.looper)
    }

    override fun open(id: Boolean, cameraOpenCallback: CameraOpenCallback) {
        mPostor.post { mImpl.open(id, cameraOpenCallback) }
    }

    override fun close() {
        mPostor.post { mImpl.close() }
    }

    override fun release() {
        mPostor.post { mImpl.release() }
        if (mCameraThread != null) {
            mCameraThread!!.quit()
            mCameraThread = null
        }
    }

    override fun setPreviewCallback(callback: PreviewCallback) {
        mPostor.post { mImpl.setPreviewCallback(callback) }
    }

    override fun setPreviewTexture(textureSupplier: Supplier<SurfaceTexture>) {
        mPostor.post { mImpl.setPreviewTexture(textureSupplier) }
    }

    override fun startPreview() {
        mPostor.post { mImpl.startPreview() }
    }

    override fun stopPreview() {
        mPostor.post { mImpl.stopPreview() }
    }

    override fun takePhoto(focus: Boolean, callback: TakePhotoCallback) {
        mPostor.post { mImpl.takePhoto(focus, callback) }
    }

    override fun setFocusArea(supplier: Reformer<List<AkCamera.Area>>) {
        mPostor.post { mImpl.setFocusArea(supplier) }
    }

    override fun setMeterArea(supplier: Reformer<List<AkCamera.Area>>) {
        mPostor.post { mImpl.setMeterArea(supplier) }
    }

    override fun setParameterReformer(reformer: Reformer<AkCamera.Parameter>, fineControl: Reformer<AkCamera.Parameter>) {
        mPostor.post { mImpl.setParameterReformer(reformer, fineControl) }
    }

    override fun focus(callback: FocusCallback) {
        mPostor.post { mImpl.focus(callback) }
    }

    override fun meter(auto: Boolean) {
        mPostor.post { mImpl.meter(auto) }
    }

    override fun applyParameter() {
        mPostor.post { mImpl.applyParameter() }
    }

}
