#extension GL_OES_EGL_image_external : require 
precision highp float;
varying vec2 sampler_vertex;
uniform samplerExternalOES texture_0;
void main() {
   gl_FragColor= texture2D(texture_0, sampler_vertex );
}