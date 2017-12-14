package com.taoisym.akmedia.camera.v1

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
import com.taoisym.akmedia.camera.*
import com.taoisym.akmedia.camera.common.Adapter
import com.taoisym.akmedia.std.Reformer
import com.taoisym.akmedia.std.Supplier
import java.io.IOException


class AkCameraV1 : AkCamera {
    protected var mCamera: Camera? = null

    protected var mReforer: Reformer<AkCamera.Parameter>? = null
    protected var mFineControl: Reformer<AkCamera.Parameter>? = null
    protected var mPreviewCallback: PreviewCallback? = null
    protected var mTarget: Supplier<SurfaceTexture>? = null
    private var mFocusAreaSupplier: Reformer<List<AkCamera.Area>>? = null
    private var mMeterAreaSupplier: Reformer<List<AkCamera.Area>>? = null
    protected var mFocusManager: FocusManager? = null

    /**
     * front true,back false
     * else null
     */
    override var face: Boolean? = null
    override val parameter: AkCamera.Parameter
        get() = if (mCamera == null) throw RuntimeException() else from(mCamera!!.parameters)


    override fun open(faceFront: Boolean, cameraOpenCallback: CameraOpenCallback) {
        face=null
        var open = -1
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (faceFront && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                open = i
                break
            }
            if (!faceFront && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                open = i
                break
            }
        }
        if (open == -1) {
            open = 0
            face = !faceFront
        } else {
            face = faceFront
        }
        try {
            val camera = Camera.open(open)
            mCamera = camera
            cameraOpenCallback?.invoke(true)
            mFocusManager = FocusManager()

        } catch (e: Exception) {
            face=null
            cameraOpenCallback?.invoke(false)
        }

    }

    override fun close() {
        mCamera?.apply {
            stopPreview()
            release()
        }
        mFocusManager?.apply {
            reset()
        }
        face=null
        mCamera = null
        mFocusManager = null
        mTarget = null
        mFocusAreaSupplier = null
        mMeterAreaSupplier = null
        mPreviewCallback = null
        mReforer = null
        mFineControl = null
    }

    override fun release() {
        close()
    }

    override fun setPreviewCallback(callback: PreviewCallback) {
        mPreviewCallback = callback
        if (callback == null) {
            mCamera!!.setPreviewCallback(null)
            return
        }
        val size = mCamera!!.parameters.previewSize
        val width = size.width
        val height = size.height
        val format = mCamera!!.parameters.previewFormat

        mCamera!!.setPreviewCallback { data, camera -> mPreviewCallback!!.invoke(data, width, height, format) }

    }

    override fun setPreviewTexture(textureSupplier: Supplier<SurfaceTexture>) {
        mTarget = textureSupplier
    }

    override fun startPreview() {
        mCamera?.apply {
            //apply parameter
            if (mReforer != null)
                mCamera!!.parameters = from(mReforer!!.apply(from(mCamera!!.parameters)))
            mCamera!!.parameters.focusMode=FOCUS_MODE_CONTINUOUS_VIDEO
            //call texture
            mTarget?.run {
                try {
                    setPreviewTexture(this.get())
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
            }
            //recall because parameter maybe changed
            mPreviewCallback?.let { setPreviewCallback(it) }
            startPreview()
            focus {}
            //apply must after preview

        }
    }

    override fun stopPreview() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
        }
        if (mFocusManager != null) {
            mFocusManager!!.reset()
        }
    }

    override fun takePhoto(focus: Boolean, callback: TakePhotoCallback) {
        if (mCamera == null)
            return
        if (focus) {
            mFocusManager!!.focusWithPicture(callback)
        } else {
            takePicture(callback)
            // TODO: 17-7-21 take photo
        }
    }

    private fun takePicture(callback: TakePhotoCallback) {
        mCamera!!.takePicture(null, Camera.PictureCallback { data, camera ->
            val size = mCamera!!.parameters.pictureSize
            val format = mCamera!!.parameters.pictureFormat
            callback.invoke(data, size.width, size.height, format)
        }, null)
    }

    override fun setFocusArea(supplier: Reformer<List<AkCamera.Area>>) {
        mFocusAreaSupplier = supplier
    }

    override fun setMeterArea(supplier: Reformer<List<AkCamera.Area>>) {
        mMeterAreaSupplier = supplier
    }

    override fun setParameterReformer(reformer: Reformer<AkCamera.Parameter>, fineControl: Reformer<AkCamera.Parameter>) {
        mReforer = reformer
        mFineControl = fineControl
    }

    override fun focus(callback: FocusCallback) {
        if (mCamera != null) {
            mFocusManager!!.focus(callback)
        }
    }

    override fun applyParameter() {
        if (mCamera != null && mFineControl != null) {
            mCamera!!.parameters = from(mFineControl!!.apply(ParameterV1(mCamera!!.parameters)))
            //todo
            //mCamera!!.parameters.setRotation()
        }
    }


    override fun meter(auto: Boolean) {
        if (mCamera == null)
            return
        if (auto) {
            val parameter = mCamera!!.parameters
            if (parameter.isAutoExposureLockSupported) {
                if (parameter.autoExposureLock) {
                    parameter.autoExposureLock = true
                }
            }
            return
        }
        if (mMeterAreaSupplier != null) {
            val parameter = mCamera!!.parameters
            parameter.meteringAreas = Adapter.arealistFromCompact(mMeterAreaSupplier!!.apply(
                    Adapter.arealistFromCamera(parameter.meteringAreas)
            ))
        }
    }


    private fun from(parameter: AkCamera.Parameter): Camera.Parameters? {
        // TODO: 17-7-21 cast
        return (parameter as ParameterV1).parameters
    }

    private fun from(parameter: Camera.Parameters): AkCamera.Parameter {
        return ParameterV1(parameter)
    }


    inner class FocusManager : Camera.AutoFocusCallback {
        @Volatile
        var focusing = false
        var mFocusCallback: FocusCallback? = null
        var mTakePhotoCallback: TakePhotoCallback? = null

        fun reset() {
            focusing = false
            mFocusCallback = null
            mTakePhotoCallback = null
        }

        override fun onAutoFocus(success: Boolean, camera: Camera) {
            focusing = false
            if (mFocusCallback != null) {
                mFocusCallback!!.invoke(success)
                mFocusCallback = null
            }
            if (mTakePhotoCallback != null) {
                takePicture(mTakePhotoCallback!!)
                mTakePhotoCallback = null
            }
        }

        fun focus(callback: FocusCallback?) {
            mFocusCallback = callback
            if (focusing) {
                callback?.invoke(false)
                return
            }
            if (mFocusAreaSupplier != null) {
                val parameter = mCamera!!.parameters
                parameter.focusAreas = Adapter.arealistFromCompact(
                        mFocusAreaSupplier!!.apply(
                                Adapter.arealistFromCamera(parameter.focusAreas)
                        )
                )
            }
            mCamera!!.autoFocus(this)
            focusing = true
        }

        fun focusWithPicture(callback: TakePhotoCallback) {
            mTakePhotoCallback = callback
            if (focusing) {
                return
            } else {
                focus(null)
            }
        }
    }

    protected inner class ParameterV1(var parameters: Camera.Parameters) : AkCamera.Parameter() {

        override val supportPreviewSize: List<AkCamera.Size>
            get() = if (parameters != null) {
                Adapter.sizelistFromCamera(parameters!!.supportedPreviewSizes)
            } else throw RuntimeException()

        override var previewSize: AkCamera.Size
            get() {
                if (parameters != null) {
                    val previewSize = parameters!!.previewSize
                    return AkCamera.Size(previewSize.width, previewSize.height)
                }
                return throw RuntimeException()
            }
            set(previewSize) {
                parameters?.apply {
                    parameters.setPreviewSize(previewSize.width, previewSize.height)
                    mCamera!!.parameters = parameters
                }
            }

        override val maxExposureCompensation: Int
            get() = parameters!!.maxExposureCompensation

        override val minExposureCompensation: Int
            get() = parameters!!.minExposureCompensation

        override val supportedWhiteBalance: List<String>
            get() = parameters!!.supportedWhiteBalance

        override val supportedSceneModes: List<String>
            get() = parameters!!.supportedSceneModes

        override var autoExposureLock: Boolean
            get() = parameters!!.autoExposureLock
            set(toggle) {
                parameters!!.autoExposureLock = toggle
            }

        override var autoWhiteBalanceLock: Boolean
            get() = parameters!!.autoWhiteBalanceLock
            set(toggle) {
                parameters!!.autoExposureLock = toggle
            }

        override fun setFocusArea(areas: List<AkCamera.Area>) {

        }

        override fun setMeterArea(areas: List<AkCamera.Area>) {

        }

        override fun setRotationHint(degree: Int) {
            if (mCamera != null) {
                parameters!!.setRotation(degree)
                mCamera!!.parameters = parameters
            }
        }

        override fun setExposureCompensation(e: Int) {
            parameters!!.exposureCompensation = e
        }

        override fun setWhiteBalance(e: String) {
            parameters!!.whiteBalance = e
        }

        override fun setSceneMode(e: String) {
            parameters!!.sceneMode = e
        }

        override fun setPictureSize(width: Int, height: Int) {
            parameters!!.setPictureSize(width, height)
        }

        override fun setPictureFormat(format: Int) {
            parameters!!.pictureFormat = format
        }

        override fun setPreviewFormat(format: Int) {
            parameters!!.previewFormat = format
        }
    }
}
