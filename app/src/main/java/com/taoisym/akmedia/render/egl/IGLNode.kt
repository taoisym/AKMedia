package com.taoisym.akmedia.render.egl


interface IGLNode {
    fun prepare(env: GLEnv)
    fun using(use: Boolean) {}
    fun release(env: GLEnv)
}
