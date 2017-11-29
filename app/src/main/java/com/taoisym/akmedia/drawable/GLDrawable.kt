package com.taoisym.akmedia.drawable

import android.opengl.GLES20
import android.opengl.Matrix
import com.taoisym.akmedia.layout.Loc
import com.taoisym.akmedia.render.TextureRender
import com.taoisym.akmedia.render.egl.GLEnv
import com.taoisym.akmedia.render.egl.GLTexture
import com.taoisym.akmedia.render.egl.GLToolkit
import com.taoisym.akmedia.render.egl.IGLNode
import com.taoisym.akmedia.std.Ref
import com.taoisym.akmedia.std.Supplier
import glm.mat4x4.Mat4
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


abstract class GLDrawable(val oes: Boolean) : IGLNode {
    var texture= Ref<GLTexture>(null)
    var locShape = Loc()
    var locTex = Loc(true)
    var mtxShape = Mat4()
    var mtxTex = Mat4()

    val id = FloatArray(16)

    private val mtxCache: FloatBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    override fun prepare(env: GLEnv) {
        Matrix.setIdentityM(id, 0)
    }

    fun update(render: TextureRender) {
        android.opengl.GLES20.glVertexAttribPointer(render.shapeId, 2, android.opengl.GLES20.GL_FLOAT, false, 8, locShape.toGL(null))
        android.opengl.GLES20.glEnableVertexAttribArray(render.shapeId)
        android.opengl.GLES20.glVertexAttribPointer(render.texId, 2, android.opengl.GLES20.GL_FLOAT, false, 8, locTex.toGL(null))
        android.opengl.GLES20.glEnableVertexAttribArray(render.texId)
    }

    override fun release(env: GLEnv) {
        texture.value?.release(env)
    }

    open fun draw(env: GLEnv, render: TextureRender?) {
        val tex=texture.value
        if (tex == null)
            return

        var used = render
        if (used == null) {
            used = if (oes) env.oes else env.tex
        }
        used.using(true)
        update(used)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(tex.type, tex.id)
        GLToolkit.checkError()
        //GLES20.glUniform1i(used.texActive, 0)
        GLToolkit.checkError()
        mtxCache.clear()
        mtxShape.to(mtxCache)
        //mtxCache.flip()
        GLES20.glUniformMatrix4fv(used.trShape, 1, false, id, 0)
        GLToolkit.checkError()
        mtxCache.clear()
        mtxTex.to(mtxCache)
        //mtxCache.flip()
        GLES20.glUniformMatrix4fv(used.trTex, 1, false, id, 0)
        GLToolkit.checkError()
        android.opengl.GLES20.glDrawArrays(android.opengl.GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLToolkit.checkError()
        used.using(false)

    }
}





