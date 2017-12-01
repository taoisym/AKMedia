package com.taoisym.akmedia.drawable

import android.graphics.Bitmap
import com.taoisym.akmedia.render.GLEnv

class BitmapDrawable(val bmp: Bitmap) : TextureDrawable(false, bmp.width, bmp.height) {
    override fun prepare(env: GLEnv) {
        super.prepare(env)
        texture.value?.update(bmp)
    }
}