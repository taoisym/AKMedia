package com.taoisym.akmedia.ui

import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by taoisym on 17-11-28.
 */
class AssetCopy {

    companion object {

        fun copyAssets(assetManager: AssetManager) {
            var files: Array<String>? = null
            try {
                files = assetManager.list("")
            } catch (e: IOException) {
                Log.e("tag", "Failed to get asset file list.", e)
            }

            if (files != null)
            for (filename in files) {
                try {

                    assetManager.open(filename)?.use { input ->
                        val output = FileOutputStream(File("/sdcard", filename))
                        output.use {
                            input.copyTo(it)
                        }
                    }
                }catch (e:Exception){

                }
            }
        }

    }
}