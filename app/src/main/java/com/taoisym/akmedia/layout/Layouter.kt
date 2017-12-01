package com.taoisym.akmedia.layout

import glm.vec4.Vec4

class Layout(srcWidth:Int,srcHeight:Int,camera:Boolean?=null){
    val locShape:Loc=Loc()
    val locTex:Loc=Loc(tex = true)

    private var height=0
    private var width=0
    private var x:Int=0
    private var y:Int=0
    init {
        locTex.camera=camera
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
        layout.locShape.corner0= Vec4(-1,-1,0,0)
        layout.locShape.corner1= Vec4(1,1,0,0)
        layout.locTex.corner0= Vec4(0,0,0,0)
        layout.locTex.corner1 =Vec4(1,1,0,0)
    }
}
class CenterCrop:Gravity(){
    override fun compute(layout: Layout) {

    }
}