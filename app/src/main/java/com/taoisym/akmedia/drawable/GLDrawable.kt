package com.taoisym.akmedia.drawable

import android.opengl.GLES20
import android.opengl.Matrix
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.render.egl.GLTexture
import com.taoisym.akmedia.render.egl.GLToolkit
import com.taoisym.akmedia.render.egl.IGLNode
import glm.mat4x4.Mat4
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


abstract class GLDrawable(val oes: Boolean) : IGLNode {
    var texture: GLTexture? = null
    private var shapeId = 0
    private var texId = 0
    private var trShape: Int = 0
    private var trTex: Int = 0
    private var texActive: Int = 0

    var locShape = Loc()
    var locTex = Loc(true)
    var mtxShape = Mat4()
    var mtxTex = Mat4()

    val id = FloatArray(16)

    private val mtxCache: FloatBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    private var render: TextureRender? = null

    override fun prepare(env: GLEnv) {
        Matrix.setIdentityM(id, 0)

        render = if (oes) env.oes else env.noes
        val id = render?.id ?: 0
        shapeId = android.opengl.GLES20.glGetAttribLocation(id, "draw_shape")
        texId = android.opengl.GLES20.glGetAttribLocation(id, "texture_vertex")
        trShape = GLES20.glGetUniformLocation(id, "tr_shape")
        trTex = GLES20.glGetUniformLocation(id, "tr_texture")
        texActive = GLES20.glGetUniformLocation(id, "texture_0")
    }

    fun update() {
        android.opengl.GLES20.glVertexAttribPointer(shapeId, 2, android.opengl.GLES20.GL_FLOAT, false, 8, locShape.toGL(null))
        android.opengl.GLES20.glEnableVertexAttribArray(shapeId)
        android.opengl.GLES20.glVertexAttribPointer(texId, 2, android.opengl.GLES20.GL_FLOAT, false, 8, locTex.toGL(null))
        android.opengl.GLES20.glEnableVertexAttribArray(texId)
    }

    override fun release(env: GLEnv) {
        texture?.release(env)
    }

    open fun draw(env: GLEnv) {
        texture?.let {
            render?.using(true)
            update()
            GLES20.glBindTexture(it.type, it.id)
            GLToolkit.checkError()
            GLES20.glUniform1i(texActive, 0)
            GLToolkit.checkError()
            mtxCache.clear()
            mtxShape.to(mtxCache)
            //mtxCache.flip()
            GLES20.glUniformMatrix4fv(trShape, 1, false, id, 0)
            GLToolkit.checkError()
            mtxCache.clear()
            mtxTex.to(mtxCache)
            //mtxCache.flip()
            GLES20.glUniformMatrix4fv(trTex, 1, false, id, 0)
            GLToolkit.checkError()
            android.opengl.GLES20.glDrawArrays(android.opengl.GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GLToolkit.checkError()
            render?.using(false)
        }
    }
}





