#version 400

in vec3 modelViewVertexNormal;
in vec3 modelViewVertexPos;
in vec2 texturePos;
in float stop;
in float color_scalar;

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

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

void setupColours(Material material, vec2 textCoord)
{
    if (material.has_diffuse_map == 1)
    {
        float x = texturePos.x / material.diffuse_map.numCols + material.diffuse_map.texXOffset;
		float y = texturePos.y / material.diffuse_map.numRows + material.diffuse_map.texYOffset;
        ambientC = texture(material.diffuse_map.texture_sampler, vec2(x, y));
        diffuseC = ambientC;
        speculrC = ambientC;
    }
    else
    {
        ambientC = vec4(material.ambientColor.xyz, material.transparency);
        diffuseC = vec4(material.diffuseColor.xyz, material.transparency);
        speculrC = vec4(material.specularColor.xyz, material.transparency);
    }
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

void main() {
	if(stop == 1) {
		discard;
	}
	
	setupColours(material, texturePos);
 
 	vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, modelViewVertexPos, modelViewVertexNormal);
	
    for (int i=0; i<maxLights; i++)
    {
        if ( pointLights[i].intensity > 0 )
        {
            diffuseSpecularComp += calcPointLight(pointLights[i], modelViewVertexPos, modelViewVertexNormal); 
        }
    }

    for (int i=0; i<maxLights; i++)
    {
        if ( spotLights[i].pl.intensity > 0 )
        {
            diffuseSpecularComp += calcSpotLight(spotLights[i], modelViewVertexPos, modelViewVertexNormal);
        }
    }
    fragColor = clamp(ambientC * vec4(ambientLight, material.transparency) + diffuseSpecularComp, 0, 1);
    if ( fog._active == 1 ) 
    {
        fragColor = calcFog(modelViewVertexPos, fragColor, fog, ambientLight, directionalLight);
    }
    
    fragColor.x *= color_scalar;
    fragColor.y *= color_scalar;
    fragColor.z *= color_scalar;
	
}
 
