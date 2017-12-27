package com.taoisym.akmedia.render.egl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.taoisym.akmedia.render.GLEnv;

import org.jetbrains.annotations.NotNull;


public class GLTexture implements IGLNode {
    public int id;
    public int type;
    public int width;
    public int height;

    public GLTexture(int type, int width, int height) {
        //GLES20.GL_TEXTURE_2D; // GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        this.type = type;//GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        this.width = width;
        this.height = height;
    }

    @Override
    public void prepare(GLEnv env) {
        if (id <= 0)
            id = GLToolkit.genTexture(type, width, height);
    }

    @Override
    public void using(boolean use) {
        if(use) {
            GLES20.glBindTexture(type, id);
        }else {
            GLES20.glBindTexture(type, 0);
        }
        GLToolkit.checkError();
    }

    @Override
    public void release(GLEnv env) {
        GLToolkit.releaseTexture(id);
    }

    public void update(@NotNull Bitmap bmp) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
    }

    @Override
    public String toString() {
        return "tex="+id+"/"+type;
    }
}
