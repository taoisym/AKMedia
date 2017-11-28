precision mediump float; 
uniform sampler2D u_Texture0; 
uniform sampler2D u_Texture1;
varying vec2 v_TexCoordinate; 
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
	 //get the pixel
     vec2 st = v_TexCoordinate.st;
     vec3 irgb = texture2D(u_Texture0, st).rgb;
     vec3 f = texture2D(u_Texture1, st).rgb;
     
     //adjust the brightness/contrast/saturation
     float T_bright = 1.2;
     float T_contrast = 1.0;
     float T_saturation = 0.9;
     vec3 bcs_result = BrightnessContrastSaturation(irgb, T_bright, T_contrast, T_saturation);
     
     //more red, less blue
     vec3 rb_result = vec3(bcs_result.r*1.1, bcs_result.g, bcs_result.b*0.9);
     
     //add f (overlay blending)
     vec3 after_f = mix(rb_result, ovelayBlender(rb_result, f), 0.5);
     	
     gl_FragColor = vec4(after_f, 1.);
}