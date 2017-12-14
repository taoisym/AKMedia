package com.taoisym.akmedia.layout

class GLTransform {
    private var width=0
    private var height=0
    private var paddingTop=0
    private var paddingBottom=0

    fun lp2wp(pos: Pos): Pos {
        return pos
    }
    fun wp2lp(pos: Pos): Pos {
        return pos
    }
    fun lp2vp(pos: Pos): Pos {
        return pos
    }
    fun vp2lp(pos: Pos): Pos {
        return pos
    }

    fun setWindowLayout(width: Int, height: Int, paddingTop: Int, paddingBottom: Int) {
        this.width=width
        this.height=height
        this.paddingTop=paddingTop
        this.paddingBottom=paddingBottom
    }
}