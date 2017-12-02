package com.taoisym.akmedia.layout

import glm.vec4.Vec4

class Layout(srcWidth:Int,srcHeight:Int,camera:Boolean?=null){
    val locShape:Loc=Loc()
    val locTex:Loc=Loc()

    private var height=0
    private var width=0
    private var x:Int=0
    private var y:Int=0
    init {
    }
    var gravity:Gravity=FillXY()
        set(value) {
            field=value
            compute()
        }
    fun setViewPort(x:Int,y:Int,width: Int,height: Int){
        this.x=x
        this.y=y
        this.width=width
        this.height=height
        compute()
    }

    private fun compute(){
        gravity.compute(this)
    }
}
sealed class Gravity{
    internal abstract fun compute(layout:Layout)
}
class FillXY:Gravity(){
    override fun compute(layout: Layout) {
    }
}
class CenterCrop:Gravity(){
    override fun compute(layout: Layout) {

    }
}