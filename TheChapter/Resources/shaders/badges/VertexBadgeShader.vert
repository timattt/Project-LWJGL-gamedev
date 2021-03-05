#version 400

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 3) in vec2 texturePosition;

out vec3 modelViewVertexNormal;
out vec3 modelViewVertexPos;
out vec2 texturePos;
out float stop;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec4 frustum[6];

void main() {
 	vec4 mpos = modelMatrix * vec4(position, 1.0);
  	
  	stop = 0;
  	for(int i = 0; i < 6; i++) {
  		if( frustum[i].x * mpos.x + frustum[i].y * mpos.y + frustum[i].z * mpos.z + frustum[i].w <= 0 ) {
  			stop = 1;
  			break;
  		}
  	}
  	
    vec4 mvPos = viewMatrix * mpos;
    gl_Position = projectionMatrix * mvPos;
    
    //	Model view
    modelViewVertexNormal = normalize(viewMatrix * vec4(normal, 0.0)).xyz;
    modelViewVertexPos = mvPos.xyz;
    
    texturePos = texturePosition;
}