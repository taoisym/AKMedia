package com.taoisym.akmedia.camera

import android.graphics.Rect
import android.graphics.SurfaceTexture

import com.taoisym.akmedia.std.Reformer
import com.taoisym.akmedia.std.Supplier
typealias PreviewCallback = (data: ByteArray, width: Int, height: Int, format: Int) -> Unit
typealias CameraOpenCallback = (success: Boolean) -> Unit
typealias FocusCallback = (success: Boolean) -> Unit
typealias TakePhotoCallback = (data: ByteArray, i: Int, width: Int, height: Int) -> Unit

interface AkCamera {

    val parameter: Parameter
    var face:Boolean?
    class Area(var rect: Rect, var weight: Int)

    class Size {
        @JvmField
        val width: Int
        @JvmField
        val height: Int

        constructor(width: Int, height: Int) {
            this.width = width
            this.height = height
        }

        constructor(from: Size) {
            this.width = from.width
            this.height = from.height
        }

    }

    abstract class Parameter {

        abstract val supportPreviewSize: List<Size>

        abstract var previewSize: Size

        abstract val maxExposureCompensation: Int

        abstract val minExposureCompensation: Int

        abstract val supportedWhiteBalance: List<String>

        abstract val supportedSceneModes: List<String>

        abstract var autoExposureLock: Boolean

        abstract var autoWhiteBalanceLock: Boolean

        abstract fun setFocusArea(areas: List<Area>)

        abstract fun setMeterArea(areas: List<Area>)
        abstract fun setRotationHint(degree: Int)

        abstract fun setExposureCompensation(e: Int)

        abstract fun setWhiteBalance(s: String)


        abstract fun setSceneMode(s: String)
        abstract fun setPictureSize(width: Int, height: Int)
        abstract fun setPictureFormat(format: Int)
        abstract fun setPreviewFormat(format: Int)
    }


    fun open(id: Boolean, cameraOpenCallback: CameraOpenCallback)

    fun close()

    fun release()

    fun setPreviewCallback(callback: PreviewCallback)

    fun setPreviewTexture(textureSupplier: Supplier<SurfaceTexture>)


    fun startPreview()

    fun stopPreview()


    fun takePhoto(focus: Boolean, callback: TakePhotoCallback)

    fun setFocusArea(supplier: Reformer<List<Area>>)

    fun setMeterArea(supplier: Reformer<List<Area>>)

    fun setParameterReformer(reformer: Reformer<Parameter>, fineControl: Reformer<Parameter>)

    fun focus(callback: FocusCallback)

    fun meter(auto: Boolean)

    fun applyParameter()
}
