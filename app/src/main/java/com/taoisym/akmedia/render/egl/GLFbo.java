package com.taoisym.akmedia.render.egl;

import android.opengl.GLES20;


public class GLFbo implements IGLNode {
    protected GLTexture texture;
    protected int fbo;

    public GLFbo(GLTexture target) {
        texture = target;//new GLTexture(GLES20.GL_TEXTURE_2D,width,height);
    }

    @Override
    public void prepare(GLEnv env) {
        fbo = GLToolkit.genFbo(texture.id);
    }

    @Override
    public void using(boolean use) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, use ? fbo : 0);
    }

    @Override
    public void release(GLEnv env) {
        GLToolkit.releaseFbo(fbo);
    }
}
