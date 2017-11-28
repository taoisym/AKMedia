precision highp float; 
varying vec2 sampler_vertex;
uniform sampler2D texture_0;
void main() {
   gl_FragColor =vec4(pow(texture2D(texture_0, sampler_vertex).rgb, vec3(1.0/2.2)),1.0);
}
