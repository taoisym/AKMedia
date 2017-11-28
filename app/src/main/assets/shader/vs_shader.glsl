attribute vec4 draw_shape;
attribute vec4 texture_vertex;
uniform mat4 tr_shape;
uniform mat4 tr_texture;
varying vec2 sampler_vertex;

void main()
{
    gl_Position = tr_shape*draw_shape;
    sampler_vertex = (tr_texture*texture_vertex).xy;
}