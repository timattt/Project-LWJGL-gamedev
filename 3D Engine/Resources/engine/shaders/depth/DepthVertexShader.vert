#version 400

layout (location = 0) in vec3 position;
layout (location = 4) in mat4 i_modelMatrix;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

uniform float chunked;

void main() {
	if (chunked > 0) {
		gl_Position = projectionMatrix * viewMatrix * i_modelMatrix * vec4(position, 1.0f);
	} else {
		gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0f);
	}
}