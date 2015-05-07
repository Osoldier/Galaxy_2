#version 120
//precision mediump float;

uniform sampler2D us2dTexture;

varying vec4 vv4Color;

void main() {
	vec4 textureColor = texture2D(us2dTexture, gl_PointCoord);
	gl_FragColor = vv4Color;
}