#version 400

out vec2 fragTexturePos;

layout (location = 0) in vec3 position;
layout (location = 3) in vec2 texturePos;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main() {
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position.xyz, 1);
	fragTexturePos = texturePos;
}