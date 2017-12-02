package com.taoisym.akmedia.layout

import glm.mat4x4.Mat4
import glm.vec2.Vec2
import glm.vec3.Vec3
import glm.vec4.Vec4
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Loc0 {

    constructor(p0: Vec2, p1: Vec2, texMirror: Boolean = false) {
        this.mirror = texMirror
        corner0 = Vec4(p0, 0,1)
        corner1 = Vec4(p1, 0f,1)
    }

    constructor(tex: Boolean = false) {
        if (tex) {
            corner0 = Vec4(0, 0, 0,1)
        } else {
            corner0 = Vec4(-1, -1, 0,1)
        }
        this.mirror = tex
        corner1 = Vec4(1, 1, 0f,1)
    }
    fun ratioSrc(hwRatio: Float, rotate:Boolean=true):Loc0{
        var mat= Mat4()
        if(rotate) {
            if (hwRatio > 1) {
                mat = mat.translate(Vec3(0.5)).scale(1 / hwRatio, 1f, 1f).translate(Vec3(-0.5))
            } else if (hwRatio < 1) {
                mat = mat.translate(Vec3(0.5)).scale(1f, 1 / hwRatio, 1f).translate(Vec3(-0.5))
            }
        }else{
            if (hwRatio > 1) {
                mat = mat.translate(Vec3(0.5)).scale(1f, 1 / hwRatio, 1f).translate(Vec3(-0.5))
            } else if (hwRatio < 1) {
                mat = mat.translate(Vec3(0.5)).scale(1 / hwRatio, 1f,1f).translate(Vec3(-0.5))
            }
        }
        corner0=mat*corner0
        corner1=mat*corner1
        return this
    }
    private var changed = true
    var mirror: Boolean = false
    var camera: Boolean?=null
    var corner0: Vec4
        set(value) {
            changed = true
            field = value
        }
    var corner1: Vec4
        set(value) {
            changed = true
            field = value
        }

    val loc = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    fun toGL(tr: GLTransform?): Buffer {
        if (!changed)
            return loc
        changed = false
        var a = if (tr != null) tr.translate(corner0) else corner0
        var b = if (tr != null) tr.translate(corner1) else corner1
        loc.position(0)
        if (mirror) {
            if(camera==null){
                loc.put(a.x)
                loc.put(b.y)
                loc.put(b.x)
                loc.put(b.y)
                loc.put(a.x)
                loc.put(a.y)
                loc.put(b.x)
                loc.put(a.y)
            }else if(camera==true) {
                loc.put(a.x)
                loc.put(b.y)
                loc.put(a.x)
                loc.put(a.y)
                loc.put(b.x)
                loc.put(b.y)
                loc.put(b.x)
                loc.put(a.y)
            }else{
                loc.put(b.x)
                loc.put(b.y)
                loc.put(b.x)
                loc.put(a.y)
                loc.put(a.x)
                loc.put(b.y)
                loc.put(a.x)
                loc.put(a.y)
            }
        } else {
            loc.put(a.x)
            loc.put(a.y)
            loc.put(b.x)
            loc.put(a.y)
            loc.put(a.x)
            loc.put(b.y)
            loc.put(b.x)
            loc.put(b.y)
        }
        loc.position(0)
        return loc
    }


}