package com.taoisym.akmedia.render.egl;

import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import javax.microedition.khronos.opengles.GL11;


@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GLToolkit {
    public static GLContext eglSetup(EGLContext share_context, Boolean recordAble) {
        int flag = recordAble ? (GLContext.FLAG_TRY_GLES3 | GLContext.FLAG_RECORDABLE) : GLContext.FLAG_TRY_GLES3;
        return new GLContext(share_context, flag);
    }

    public static int complie(String vs, String fs) {
        int vs_id = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        int fs_id = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(vs_id, vs);
        checkError();
        GLES20.glCompileShader(vs_id);
        checkError();
        GLES20.glShaderSource(fs_id, fs);
        checkError();
        GLES20.glCompileShader(fs_id);
        checkError();
        int id = GLES20.glCreateProgram();
        GLES20.glAttachShader(id, vs_id);
        checkError();
        GLES20.glAttachShader(id, fs_id);
        checkError();
        GLES20.glLinkProgram(id);

        checkError();
        return id;
    }

    //= GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    public static int genTexture(int type, int width, int height) {
        int[] id = new int[1];
        GLES20.glGenTextures(1, id, 0);
        checkError();
        GLES20.glBindTexture(type, id[0]);
        checkError();
        GLES20.glTexParameteri(type, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(type, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GLES20.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(type, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        if (width > 0 && height > 0 && type == GLES20.GL_TEXTURE_2D) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        }
        return id[0];
    }

    public static void releaseTexture(int texture) {
        GLES20.glDeleteTextures(1, new int[]{texture}, 0);
    }

    public static void useProgram(int program) {
        GLES20.glUseProgram(program);
        checkError();
    }

    public static void deleteProgram(int program) {
        GLES20.glDeleteProgram(program);
    }

    public static void checkError() {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String e = "error:"+error + GLES20.glGetString(error);
            Log.e("GLE", e);
            throw new RuntimeException(e);
        }
    }

    public static int genFbo(int texture) {
        int[] ids = new int[1];
        int fbo;
        GLES20.glGenFramebuffers(1, ids, 0);
        checkError();
        fbo = ids[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture, 0);
        checkError();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return fbo;
    }

    public static void releaseFbo(int fbo) {
        GLES20.glDeleteFramebuffers(1, new int[]{fbo}, 0);
    }
}
