precision highp float;
uniform sampler2D texture_0;
uniform sampler2D texture_1;
varying vec2 sampler_vertex; 
const vec3 W = vec3(0.2125, 0.7154, 0.0721);

vec3 BrightnessContrastSaturation(vec3 color, float brt, float con, float sat)
{
	vec3 black = vec3(0., 0., 0.);
	vec3 middle = vec3(0.5, 0.5, 0.5);
	float luminance = dot(color, W);
	vec3 gray = vec3(luminance, luminance, luminance);
	
	vec3 brtColor = mix(black, color, brt);
	vec3 conColor = mix(middle, brtColor, con);
	vec3 satColor = mix(gray, conColor, sat);
	return satColor;
}

vec3 multiplyBlender(vec3 Color, vec3 f){
	vec3 f_result;
	float luminance = dot(f, W);
	
	if(luminance < 0.5)
		f_result = 2. * f * Color;
	else
		f_result = Color;
			
	return f_result;
}

vec3 ovelayBlender(vec3 Color, vec3 f){
	vec3 f_result;

	float luminance = dot(f, W);
	
	if(luminance < 0.5)
		f_result = 2. * f * Color;
	else
		f_result = 1. - (1. - (2. *(f - 0.5)))*(1. - Color);
		
	return f_result;
}

void main()
{
     vec2 st = sampler_vertex.st;
     vec3 irgb = texture2D(texture_0, st).rgb;
     vec3 f = texture2D(texture_1, st).rgb;
     
     float T_bright = 1.2;
     float T_contrast = 1.2;
     float T_saturation = 1.3;
     vec3 bcs_result = BrightnessContrastSaturation(irgb, T_bright, T_contrast, T_saturation);
     
     
     //add f (overlay blending)
     vec3 after_f = mix(bcs_result, multiplyBlender(bcs_result, f), 0.7);
     	
     gl_FragColor = vec4(after_f, 1.);
}