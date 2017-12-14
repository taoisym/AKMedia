package com.taoisym.akmedia.render.egl;


import com.taoisym.akmedia.render.GLEnv;

public class GLProgram implements IGLNode {
    public int id;
    String vs;
    String fs;

    public GLProgram(String vs, String fs) {
        this.vs = vs;
        this.fs = fs;
    }

    @Override
    public void prepare(GLEnv env) {
        id = GLToolkit.complie(vs, fs);
        GLToolkit.checkError();
    }

    @Override
    public void using(boolean use) {
        GLToolkit.useProgram(use ? id : 0);
    }

    @Override
    public void release(GLEnv env) {
        GLToolkit.deleteProgram(id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
