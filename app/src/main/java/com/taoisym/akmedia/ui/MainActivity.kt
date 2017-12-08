package com.taoisym.akmedia.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.taoisym.akmedia.R
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        AssetCopy.copyAssets(this.assets)

        camera.setOnClickListener{
            startActivity(Intent(this@MainActivity,CameraDriveActivity::class.java))
        }
        video.setOnClickListener{
            startActivity(Intent(this@MainActivity,VideoDriveActivity::class.java))
        }
    }
}
