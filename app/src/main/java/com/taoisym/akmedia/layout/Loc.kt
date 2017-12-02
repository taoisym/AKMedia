package com.taoisym.akmedia.layout

import android.graphics.Matrix
import com.taoisym.akmedia.codec.VideoDir
import glm.mat4x4.Mat4
import glm.vec2.Vec2
import glm.vec3.Vec3
import glm.vec4.Vec4
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Loc{
    private var tex=false
    /**
     * 0 X=1 Y=2 XY=3
     * Video=2,BackCamera=1,FrontCamera=3
     */
    private var flipXY: VideoDir=VideoDir.FLIP_Y
    private var rotate: Boolean=false
    private var hwRatio: Float=1f
    private val loc = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    /**
     * constructor for position
     */
    constructor(p0: Vec2=Vec2(-1), p1: Vec2=Vec2(1)){
        set(p0,p1)
    }

    /**
     *              Mp4Video FrontCamera BackCamera
     * flipXY :     2        3            1
     * rotate:      false    true        true
     *
     * center crop fill view
     * hwRatio=     (viewH/W)/(srcH/W)
     */
    constructor(flipXY:VideoDir, rotate: Boolean=false, hwRatio:Float=1f){
        tex=true
        this.flipXY=flipXY
        this.rotate=rotate
        this.hwRatio=hwRatio
        set(Vec2(0),Vec2(1))
    }
    fun set(p0: Vec2, p1: Vec2){
        var dots=FloatArray(8)
        dots[0]=p0.x
        dots[1]=p1.y

        dots[2]=p1.x
        dots[3]=p1.y

        dots[4]=p0.x
        dots[5]=p0.y

        dots[6]=p1.x
        dots[7]=p0.y
        if(tex){
            comupte(dots)
        }
        fillGL(dots)
    }
    fun toGL(): FloatBuffer {
        return loc
    }
    fun comupte(dots: FloatArray){
        var mat= Matrix()
        mat.postTranslate(-0.5f,-0.5f)
        when(flipXY){
            VideoDir.FLIP_X->mat.postScale(-1f,1f)
            VideoDir.FLIP_Y->mat.postScale(1f,-1f)
            VideoDir.FLIP_XY->mat.postScale(-1f,-1f)
        }

        if(rotate) {
            mat.postRotate(90f, 0.0f, 0.0f)
        }
        centerCrop(mat, hwRatio,rotate)
        mat.postTranslate(0.5f,0.5f)
        mat.mapPoints(dots)
        fillGL(dots)
    }
    private fun fillGL(dots:FloatArray){
        loc.position(0)
        loc.put(dots)
        loc.position(0)
    }
    private fun centerCrop(mat:Matrix, hwRatio: Float, rotate:Boolean=true) {
        if(rotate) {
            if (hwRatio > 1) {
                mat.postScale(1 / hwRatio, 1f)
            } else if (hwRatio < 1) {
                mat.postScale(1f, 1 / hwRatio)
            }
        }else{
            if (hwRatio > 1) {
                mat.postScale(1f, 1 / hwRatio)
            } else if (hwRatio < 1) {
                mat.postScale(1 / hwRatio, 1f)
            }
        }
    }
}