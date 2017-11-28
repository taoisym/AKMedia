package com.taoisym.akmedia.drawable

/**
 * Created by taoisym on 22/11/17.
 */
interface PlayAble {
    fun start()
    fun stop()
    fun pause()
    fun resume()
    fun seek(pts: Long)
}