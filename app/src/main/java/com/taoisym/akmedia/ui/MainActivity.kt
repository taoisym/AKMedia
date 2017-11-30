package com.taoisym.akmedia.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import com.taoisym.akmedia.R
import com.taoisym.akmedia.camera.CompactCamera
import com.taoisym.akmedia.std.Supplier
import com.taoisym.akmedia.video.RealSurface

import java.util.concurrent.Executors
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val camera = CompactCamera()
    val e = Executors.newSingleThreadExecutor()
    val test = CamTest()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        output.holder.addCallback(Call())
        start.setOnClickListener { test.start(this) }
        end.setOnClickListener { test.stop() }
        output.setOnClickListener { camera.focus { } }
        filter.setOnClickListener{
            test.change(this)
        }
        FileCopy.copyAssets(this.assets)
    }

    inner class Call : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: android.view.SurfaceHolder?, format: Int, width: Int, height: Int) {
            sp.set(RealSurface(holder!!.surface, width, height))
        }

        override fun surfaceDestroyed(holder: android.view.SurfaceHolder?) {
            camera.stopPreview()
            sp.set(null)
        }

        override fun surfaceCreated(holder: android.view.SurfaceHolder?) {

        }
    }

    var sp = Supplier<RealSurface>()
    override fun onResume() {
        camera.open(false, {
            val size = camera.parameter.supportPreviewSize.get(0)
            camera.parameter.previewSize = size
            test.test(camera, sp)
        })

        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        camera.close()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
