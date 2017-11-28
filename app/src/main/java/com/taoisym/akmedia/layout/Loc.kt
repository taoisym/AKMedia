package com.taoisym.akmedia.layout

import glm.vec2.Vec2
import glm.vec4.Vec4
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Loc {

    constructor(p0: Vec2, p1: Vec2, texMirror: Boolean = false) {
        this.mirror = texMirror
        corner0 = Vec4(p0, 0, 1)
        corner1 = Vec4(p1, 0f, 1f)
    }

    constructor(tex: Boolean = false) {
        if (tex) {
            corner0 = Vec4(0, 0, 0, 1)
        } else {
            corner0 = Vec4(-1, -1, 0, 1)
        }
        this.mirror = tex
        corner1 = Vec4(1, 1, 0f, 1f)
    }


    private var changed = true
    var mirror: Boolean = false
    var rotation: Int=0
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
            if(rotation==90) {
                loc.put(a.x)
                loc.put(b.y)
                loc.put(a.x)
                loc.put(a.y)
                loc.put(b.x)
                loc.put(b.y)
                loc.put(b.x)
                loc.put(a.y)


            }else{
                loc.put(a.x)
                loc.put(b.y)
                loc.put(b.x)
                loc.put(b.y)
                loc.put(a.x)
                loc.put(a.y)
                loc.put(b.x)
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