package graphicsSupport.effects;

import graphicsSupport.Item;
import graphicsSupport.mesh.Mesh;

public class SkyBox extends Item {

	// Night coefficient
	private float nightCoefficient = 1f;
	
	public SkyBox(Mesh shape) {
		super(new Mesh[] { shape });
		setScale(10);
	}

	public SkyBox(Mesh[] shape) {
		super(shape);
		setScale(10);
	}

	public final float getNightCoefficient() {
		return nightCoefficient;
	}

	public final void setNightCoefficient(float nightCoefficient) {
		this.nightCoefficient = nightCoefficient;
	}
}
