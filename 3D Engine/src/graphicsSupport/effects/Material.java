package graphicsSupport.effects;

import org.joml.Vector3f;

import graphicsSupport.texture.Texture;

public class Material {

	//	Refraction
	//
	private float refraction;//	преломление
	
	//	Texture
	private Texture texture;
	
    //	Sppecular factor
    private float reflectance;//	фактор отражения
  
    //	Specular color
    private Vector3f specularColor = new Vector3f();//	цвет зеркального отражения
    
	//	Name
	public final String name;    
    
    //	Illumination
	private int illuminationType;//		тип освещения
		
    //	Transparency
    private float transparency = 1f;//	прозрачность
    
    //	Ambient color
    private Vector3f ambientColor = new Vector3f();//	стандартный цвет
    
    //	Diffuse color
    private Vector3f diffuseColor = new Vector3f();//	цвет под освещением
    
    public Material(String name) {
    	this.name = name;
    }
  
    public final float getReflectance() {
		return reflectance;
	}

	public final float getTransparency() {
		return transparency;
	}

	public final void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	public final void setReflectance(float specularFactor) {
		this.reflectance = specularFactor;
	}

	public final Vector3f getAmbientColor() {
		return ambientColor;
	}
    
	public final Vector3f getSpecularColor() {
		return specularColor;
	}

	public final void setSpecularColor(Vector3f specularColor) {
		this.specularColor = specularColor;
	}

	public final Vector3f getDiffuseColor() {
		return diffuseColor;
	}

	public final void setDiffuseColor(Vector3f diffuseColor) {
		this.diffuseColor = diffuseColor;
	}

	public final void setAmbientColor(Vector3f ambientColor) {
		this.ambientColor = ambientColor;
	}

	public final int getIlluminationType() {
		return illuminationType;
	}

	public final void setIlluminationType(int illuminationType) {
		this.illuminationType = illuminationType;
	}

	public final float getRefraction() {
		return refraction;
	}

	public final void setRefraction(float refraction) {
		this.refraction = refraction;
	}

	public final Texture getTexture() {
		return texture;
	}

	public final void setTexture(Texture texture) {
		this.texture = texture;
	}
	
	public final boolean hasDiffuseTexture() {
		return texture != null;
	}

	@Override
	public String toString() {
		return "Material [refraction=" + refraction + ", texture=" + texture + ", specularFactor=" + reflectance
				+ ", specularColor=" + specularColor + ", name=" + name + ", illuminationType=" + illuminationType
				+ ", transparency=" + transparency + ", ambientColor=" + ambientColor + ", diffuseColor=" + diffuseColor
				+ "]";
	}

	
    
}