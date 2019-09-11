#version 440

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 3) in vec2 texturePosition;
layout (location = 4) in float visib;
layout (location = 5) in vec3 color;
layout (location = 6) in float textureIndex1;
layout (location = 7) in float angle1;
layout (location = 8) in float textureIndex2;
layout (location = 9) in float angle2;

const int split_quant = 4;

out vec3 modelViewVertexNormal;
out vec3 modelViewVertexPos;
out vec2 texturePos;
out float texture_index[2];
out float stop;
out float texture_angle[2];
out float visibility;
out vec3 _position;
out mat4 mvm;
out vec4 modelLightViewVertexPos[split_quant];
out vec3 tile_color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec4 frustum[6];

uniform mat4 lightViewMatrix[split_quant];
uniform mat4 orthoProjectionMatrix[split_quant];

void main() {
 	vec4 mpos = vec4(position, 1.0);
  	
  	stop = 0;
  	for(int i = 0; i < 6; i++) {
  		if( frustum[i].x * mpos.x + frustum[i].y * mpos.y + frustum[i].z * mpos.z + frustum[i].w <= -1) {
  			stop = 1;
  			break;
  		}
  	}
  	
  	_position = position;
  	
    vec4 mvPos = viewMatrix * mpos;
    gl_Position = projectionMatrix * mvPos;
    
    //	Model view
    modelViewVertexNormal = normalize(viewMatrix * vec4(normal, 0.0)).xyz;
    modelViewVertexPos = mvPos.xyz;
    
    texturePos = texturePosition;
	texture_index = float[2](textureIndex1, textureIndex2);
	texture_angle = float[2](angle1, angle2);
	
	mvm = viewMatrix;
	
	for (int i = 0; i < split_quant; i++) {
		modelLightViewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * vec4(position, 1.0);
	}
	
	visibility = visib;
	tile_color = color;
} 
