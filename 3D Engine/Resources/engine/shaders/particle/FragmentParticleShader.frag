#version 400

in vec2 fragTexturePos;

out vec4 fragColor;

struct Texture
{
	sampler2D texture_sampler;
	float texXOffset;
	float texYOffset;
	int numCols;
	int numRows;
};

uniform Texture frag_texture;
uniform float alpha;

void main() {
	float x = fragTexturePos.x / frag_texture.numCols + frag_texture.texXOffset;
	float y = fragTexturePos.y / frag_texture.numRows + frag_texture.texYOffset;

	fragColor = texture(frag_texture.texture_sampler, vec2(x, y));
	
	fragColor.w = min(fragColor.w, alpha);
}