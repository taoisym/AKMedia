package com.taoisym.akmedia.render

import android.opengl.GLES20
import com.taoisym.akmedia.render.egl.GLProgram
import com.taoisym.akmedia.render.egl.IGLNode

open class TextureRender : IGLNode {
    private lateinit var program: GLProgram
    internal var clear = FloatArray(4)
    var shapeId = 0
    var texId = 0
    var trShape: Int = 0
    var trTex: Int = 0
    var texActive: Int = 0

    val id by lazy { program.id }

    constructor(oes: Boolean) {
        program = GLProgram(VS, if (oes) FS_OES else FS)
        clear = floatArrayOf(0f, 0f, 0f, 1f)
    }
    constructor(vs:String,fs:String) {
        program = GLProgram(vs,fs)
        clear = floatArrayOf(0f, 0f, 0f, 1f)
    }

    constructor() {
        program = GLProgram(VS, FS_OES)
        clear = floatArrayOf(0f, 0f, 0f, 1f)
        GLES20.glClearColor(clear[0], clear[1], clear[2], clear[3])
    }

    fun clearColor(color: FloatArray) {
        if (color.size != 4) {
            return
        }
        System.arraycopy(color, 0, this.clear, 0, 4)
        GLES20.glClearColor(clear[0], clear[1], clear[2], clear[3])
    }

    override fun prepare(env: GLEnv) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glClearColor(clear[0], clear[1], clear[2], clear[3])
        program.prepare(env)
        GLES20.glUseProgram(0)
        program.using(true)
        shapeId = android.opengl.GLES20.glGetAttribLocation(id, "draw_shape")
        texId = android.opengl.GLES20.glGetAttribLocation(id, "texture_vertex")
        trShape = GLES20.glGetUniformLocation(id, "tr_shape")
        trTex = GLES20.glGetUniformLocation(id, "tr_texture")
        texActive = GLES20.glGetUniformLocation(id, "texture_0")
        program.using(false)
    }

    override fun using(use: Boolean) {
        program.using(use)
    }

    override fun release(env: GLEnv) {
        program.release(env)
    }


    companion object {

        internal val VS = "attribute vec4 draw_shape;" +
                "attribute vec4 texture_vertex;" +
                "uniform mat4 tr_shape;" +
                "uniform mat4 tr_texture;\n" +
                "varying vec2 sampler_vertex;" +
                "void main()" +
                "{" +
                "gl_Position = tr_shape*draw_shape;" +
                "sampler_vertex = (tr_texture*texture_vertex).xy;" +
                "}"
        internal val FS = "precision highp float;\n" +
                "varying vec2 sampler_vertex;                            \n" +
                "uniform sampler2D texture_0;               \n" +
                "void main() { \n" +
                "   gl_FragColor= texture2D(texture_0, sampler_vertex );\n" +
                "}"

        internal val FS_OES = "#extension GL_OES_EGL_image_external : require\n" +
                "precision highp float;\n" +
                "varying vec2 sampler_vertex;                            \n" +
                "uniform samplerExternalOES texture_0;               \n" +
                "void main() { \n" +
                "   gl_FragColor= texture2D(texture_0, sampler_vertex );\n" +
                "}"
    }

    /**
     * "   gl_FragColor= texture2D(texture_oes, sample_cord );\n" +
     * float gamma = 2.2;
     * fragColor.rgb = pow(fragColor.rgb, vec3(1.0/gamma));
     */
}