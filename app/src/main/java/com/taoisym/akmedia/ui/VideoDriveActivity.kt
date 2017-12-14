package com.taoisym.akmedia.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import com.taoisym.akmedia.R
import com.taoisym.akmedia.std.Supplier
import com.taoisym.akmedia.video.RealSurface
import kotlinx.android.synthetic.main.activity_main.*

class VideoDriveActivity : AppCompatActivity() {
    val test = UseageSample()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        output.holder.addCallback(Call())
        start.setOnClickListener { test.start(this) }
        end.setOnClickListener { test.stop() }
        output.setOnClickListener {  }
        filter.setOnClickListener{
            test.change(this)
        }
        AssetCopy.copyAssets(this.assets)
    }

    inner class Call : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: android.view.SurfaceHolder?, format: Int, width: Int, height: Int) {
            sp.set(RealSurface(holder!!.surface, width, height))
        }

        override fun surfaceDestroyed(holder: android.view.SurfaceHolder?) {
            sp.set(null)
        }

        override fun surfaceCreated(holder: android.view.SurfaceHolder?) {

        }
    }

    var sp = Supplier<RealSurface>()
    override fun onResume() {
        super.onResume()
        test.video("/sdcard/girl.mp4",sp)
    }

    override fun onPause() {
        test.release()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
