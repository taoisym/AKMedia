package com.taoisym.akmedia.codec


interface IMediaStateListener {
    fun onPause(pts: Long)
    fun onResume(pts: Long)
    fun onSeek(pts: Long)
    fun onStart()
    fun onStop()
}
