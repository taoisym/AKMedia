package com.taoisym.akmedia.camera.common

import android.hardware.Camera
import com.taoisym.akmedia.camera.AkCamera
import java.util.*


object Adapter {
    fun sizelistFromCamera(supportedPreviewSizes: List<Camera.Size>): List<AkCamera.Size> {
        val ret = ArrayList<AkCamera.Size>(supportedPreviewSizes.size)
        supportedPreviewSizes.forEach {
            ret.add(AkCamera.Size(it.width, it.height))
        }
        return ret
    }

    fun arealistFromCompact(areas: List<AkCamera.Area>?): List<Camera.Area>? {
        if (areas == null)
            return null
        val ret = ArrayList<Camera.Area>(areas.size)
        for (area in areas) {
            ret.add(Camera.Area(area.rect, area.weight))
        }
        return ret
    }

    fun arealistFromCamera(areas: List<Camera.Area>?): List<AkCamera.Area>? {
        if (areas == null)
            return null
        val ret = ArrayList<AkCamera.Area>(areas.size)
        for (area in areas) {
            ret.add(AkCamera.Area(area.rect, area.weight))
        }
        return ret
    }
}
