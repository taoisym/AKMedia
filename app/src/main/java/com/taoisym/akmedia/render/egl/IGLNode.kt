package com.taoisym.akmedia.render.egl

import com.taoisym.akmedia.render.GLEnv


interface IGLNode {
    fun prepare(env: GLEnv)
    fun using(use: Boolean) {}
    fun release(env: GLEnv)
}
