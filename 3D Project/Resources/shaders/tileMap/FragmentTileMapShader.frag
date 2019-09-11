#version 400

const int split_quant = 4;

in vec3 modelViewVertexNormal;
in vec3 modelViewVertexPos;
in vec2 texturePos;
in float texture_index[2];
in float texture_angle[2];
in float stop;
in float visibility;
in vec3 _position;
in mat4 mvm;
in vec4 modelLightViewVertexPos[split_quant];
in vec3 tile_color;

out vec4 fragColor;

struct Texture
{
	sampler2D texture_sampler;
	float texXOffset;
	float texYOffset;
	int numCols;
	int numRows;
};
struct Fog 
{
	int _active;
	vec3 color;
	int exponent;
	float density;
};
struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};
struct PointLight
{
    vec3 colour;
    // Light position is assumed to be in view coordinates
    vec3 position;
    float intensity;
    Attenuation att;
};
struct SpotLight
{
    PointLight pl;
    vec3 conedir;
    float cutoff;
};
struct Material
{
	float transparency;
	vec3 ambientColor;
	vec3 diffuseColor;
	vec3 specularColor;
	float reflectance;
	Texture diffuse_map;
	float has_diffuse_map;
};

struct DirectionalLight 
{
	vec3 colour;
    vec3 direction;
    float intensity;
};

const int maxLights = 10;

uniform vec3 ambientLight;
uniform Material material;
uniform float specularPower;
uniform PointLight pointLights[maxLights];
uniform DirectionalLight directionalLight;
uniform SpotLight spotLights[maxLights];
uniform Fog fog;

uniform sampler2D normalMap;

uniform sampler2D texture0;
uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;

uniform float splits[split_quant];
uniform sampler2D shadowMap0;
uniform sampler2D shadowMap1;
uniform sampler2D shadowMap2;
uniform sampler2D shadowMap3;

uniform float map_grid;
uniform float max_textures;

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

uniform sampler2D shadowMap;

float calcShadow(vec4 position, int map)
{
    vec3 projCoords = position.xyz;
    // Transform from screen coordinates to texture coordinates
    projCoords = projCoords * 0.5 + 0.5;
    float bias = 0.0;

    float shadowFactor = 0.0;
    vec2 inc;
    if (map == 0) {
    	inc = 1.0 / textureSize(shadowMap0, 0);
    }
    if (map == 1) {
    	inc = 1.0 / textureSize(shadowMap1, 0);
    }
    if (map == 2) {
    	inc = 1.0 / textureSize(shadowMap2, 0);
    }
    if (map == 3) {
    	inc = 1.0 / textureSize(shadowMap3, 0);
    }
    for(int row = -1; row <= 1; ++row)
    {
        for(int col = -1; col <= 1; ++col)
        {
            float textDepth;
            
            if (map == 0) {
            	textDepth = texture(shadowMap0, projCoords.xy + vec2(row, col) * inc).r; 
            }
            if (map == 1) {
            	textDepth = texture(shadowMap1, projCoords.xy + vec2(row, col) * inc).r; 
            } 
            if (map == 2) {
            	textDepth = texture(shadowMap2, projCoords.xy + vec2(row, col) * inc).r; 
            }
            if (map == 3) {
            	textDepth = texture(shadowMap3, projCoords.xy + vec2(row, col) * inc).r; 
            } 
            
            shadowFactor += projCoords.z - bias > textDepth ? 1.0 : 0.0;        
        }    
    }
    shadowFactor /= 9.0;

    if(projCoords.z > 1.0)
    {
        shadowFactor = 1;
    }

    return 1 - shadowFactor;
}

void setupColours(Material material, vec2 textCoord)
{
    	
		vec4 texture_color = vec4(0, 0, 0, 0); 
		
		for (int i = 0; i < 2; i++) {
		
		float code = texture_index[i];
		float angle_code = texture_angle[i];
		
		float index;
		float angle;
		
		index = max_textures;

		
		for (int i = 0; i < max_textures; i++) {
		if (code == 1.0) {
			break;
		}
		
		vec2 tp = vec2(clamp(texturePos.x, 0, 1), clamp(texturePos.y, 0, 1));
	
		index = code - floor(code / 100) * 100;
		angle = angle_code - floor(angle_code / 100) * 100;
		
		code = (code - index) / 100.0;
		angle_code = (angle_code - angle) / 100;
		
		angle*=10;
		
		if (angle != 0) {
			tp.x -= 0.5;
			tp.y -= 0.5;
			
			float angle_rad = angle / 180 * 3.1415926;
			
			mat2 rot = mat2(cos(angle_rad), - sin(angle_rad), sin(angle_rad), cos(angle_rad));
			
			tp = rot * tp;
			
			tp.x+=0.5;
			tp.y+=0.5;
		}
		
		vec4 tc = vec4(0, 0, 0, 0);
		if(index == 1.0) {
			tc = texture(texture1, tp);
		}
		if(index == 2.0) {
			tc = texture(texture2, tp);
		}
		if(index == 3.0) {
			tc = texture(texture3, tp);
		}
		if (tc.w > texture_color.w) {
			texture_color = tc;
		}
		
		}
		

    	
    	}
    
    
   		if (texture_color.w < 1) {
   			texture_color = vec4(tile_color.xyz, 1);
   		}
    	if(map_grid == 1) {
			texture_color = texture_color + texture(texture0, texturePos);
		}
        ambientC = texture_color;
        diffuseC = ambientC;
        speculrC = ambientC;

}

vec4 calcLightColour(vec3 light_colour, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColour = vec4(0, 0, 0, 0);
    vec4 specColour = vec4(0, 0, 0, 0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColour = diffuseC * vec4(light_colour, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * light_intensity  * specularFactor * material.reflectance * vec4(light_colour, 1.0);

    return (diffuseColour + specColour);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_colour = calcLightColour(light.colour.xyz, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_colour / attenuationInv;
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.pl.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec3 from_light_dir  = -to_light_dir;
    float spot_alfa = dot(from_light_dir, normalize(light.conedir));
    
    vec4 colour = vec4(0, 0, 0, 0);
    
    if ( spot_alfa > light.cutoff ) 
    {
        colour = calcPointLight(light.pl, position, normal);
        colour *= (1.0 - (1.0 - spot_alfa)/(1.0 - light.cutoff));
    }
    return colour;    
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
    return calcLightColour(light.colour.xyz, light.intensity, position, normalize(light.direction), normal);
}

vec4 calcFog(vec3 pos, vec4 colour, Fog fog, vec3 ambientLight, DirectionalLight dirLight)
{
    vec4 fogColor = vec4((fog.color * (ambientLight + dirLight.colour * dirLight.intensity)).xyz, 1);
    float distance = length(pos);
    float fogFactor = 1.0 / exp( pow((distance * fog.density), fog.exponent) );
    fogFactor = clamp( fogFactor, 0.0, 1.0 );

    vec3 resultColour = mix(fogColor.xyz, colour.xyz, fogFactor);
    return vec4(resultColour.xyz, colour.w);
}

vec3 calcNormal(Material material, vec3 normal, vec2 text_coord, mat4 modelViewMatrix)
{
    vec3 newNormal = normal;
    newNormal = texture(normalMap, text_coord).rgb;
    newNormal = normalize(newNormal * 2 - 1);
    newNormal = normalize(modelViewMatrix * vec4(newNormal, 0.0)).xyz;
    
    return normal;
}

void main() {
	if(stop == 1 || visibility == 0) {
		discard;
	}
	
	vec3 newNormal = calcNormal(material, modelViewVertexNormal, texturePos, mvm);
	
	setupColours(material, texturePos);
 
 	vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, modelViewVertexPos, newNormal);
	
    for (int i=0; i<maxLights; i++)
    {
        if ( pointLights[i].intensity > 0 )
        {
            diffuseSpecularComp += calcPointLight(pointLights[i], modelViewVertexPos, newNormal); 
        }
    }

    for (int i=0; i<maxLights; i++)
    {
        if ( spotLights[i].pl.intensity > 0 )
        {
            diffuseSpecularComp += calcSpotLight(spotLights[i], modelViewVertexPos, newNormal);
        }
    }
    
    int split_i = 3;
    for (int i = 0; i < split_quant; i++) {
    	if (abs(modelViewVertexPos.z) < splits[i]) {
    		split_i = i;
    		break;
    	}
    }
    float shadow = splits[0] > 0 ? calcShadow(modelLightViewVertexPos[split_i], split_i) : 1.0;
    fragColor = clamp(ambientC * vec4(ambientLight, material.transparency) + diffuseSpecularComp * shadow, 0, 1);
    if ( fog._active == 1 ) 
    {
        fragColor = calcFog(modelViewVertexPos, fragColor, fog, ambientLight, directionalLight);
    }
	
	if (visibility == 1) {
		fragColor.x *= 0.5;
		fragColor.y *= 0.5;
		fragColor.z *= 0.5;
	} 
}
 
