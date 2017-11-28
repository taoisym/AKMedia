package com.taoisym.akmedia.ui

import android.content.res.AssetManager
import android.util.Log
import java.io.*


/**
 * Created by taoisym on 17-11-28.
 */
class FileCopy {

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
                    var from: InputStream? = null
                    var out: OutputStream? = null
                    try {
                        from = assetManager.open(filename)
                        val outFile = File("/sdcard", filename)
                        out = FileOutputStream(outFile)
                        copyFile(from, out)
                    } catch (e: IOException) {
                    } finally {
                        if (from != null) {
                            try {
                                from!!.close()
                            } catch (e: IOException) {
                                // NOOP
                            }

                        }
                        if (out != null) {
                            try {
                                out!!.close()
                            } catch (e: IOException) {
                                // NOOP
                            }

                        }
                    }
                }
        }


        private fun copyFile(from: InputStream, out: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            read = from.read(buffer)
            while (read != -1) {
                out.write(buffer, 0, read)
                read = from.read(buffer)
            }
        }
    }
}