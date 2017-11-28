package com.taoisym.akmedia.codec

interface IMediaSource<Data, Target> {

    fun seek(pts: Long, flag: Int)
    /** when pass on media source
     * flag=0 is video channel,flag==1 is audio chnnale
     */
    fun addSink(pass: IMediaTargetSink<Data, Target>, flag: Int)

    fun delSink(pass: IMediaTargetSink<Data, Target>) {}
}


