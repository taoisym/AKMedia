package com.taoisym.akmedia.codec

interface IMediaSource<Data, Target> {

    fun seek(pts: Long, flag: Int)
    /** when pass on media source
     * flag=0 is mVideo channel,flag==1 is audio chnnale
     */
    fun addSink(pass: IMediaTarget<Data, Target>, flag: Int)

    fun delSink(pass: IMediaTarget<Data, Target>) {}
}


