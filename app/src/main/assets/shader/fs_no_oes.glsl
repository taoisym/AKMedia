precision highp float; 
varying vec2 sampler_vertex;
uniform sampler2D texture_0;
void main() {
   gl_FragColor =texture2D(texture_0, sampler_vertex);
}
