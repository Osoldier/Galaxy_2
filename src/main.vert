#version 120
//precision highp float;

attribute vec3 av3VertexPosition;
attribute vec3 av3VertexColor;
attribute float afPointSize;

uniform mat4 pr_matrix;

varying vec4 vv4Color;

void main() {
	gl_Position = pr_matrix * vec4(av3VertexPosition, 1.0);
	gl_PointSize = afPointSize;
	vv4Color = vec4(av3VertexColor, 1.0);
}