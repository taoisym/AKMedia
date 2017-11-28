package com.taoisym.akmedia.codec

import com.taoisym.akmedia.codec.avc.MediaSource


interface IMediaControl {

    var rangeStart: Long

    var rangeEnd: Long

    val metainfo: AvcFileMeta?
    @Throws(MediaSource.MediaException::class)
    fun start()

    fun stop()

    fun pause()

    fun resume()

    fun seek(pts: Long)

    fun setPlayRange(from: Long, to: Long)
}
