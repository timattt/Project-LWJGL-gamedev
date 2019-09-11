package graphicsSupport.effects;

import org.joml.Vector3f;

public class Fog {

	//	Color
	private Vector3f color;
	
	//	Draw parameters
	private int exponent;
	private float density;
	
	public Fog(Vector3f color, int exponent, float density) {
		this.color = color;
		this.exponent = exponent;
		this.density = density;
	}

	public final Vector3f getColor() {
		return color;
	}

	public final int getExponent() {
		return exponent;
	}

	public final float getDensity() {
		return density;
	}
	
	

}
