#version 400

out vec2 fragTexturePos;

layout (location = 0) in vec3 position;
layout (location = 3) in vec2 texturePos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
	vec4 pos = vec4(position.xyz, 0);
	pos = pos * 10;
	pos = viewMatrix * pos;
	gl_Position = projectionMatrix * vec4(pos.xyz, 1);
	fragTexturePos = texturePos;
}