package graphicsSupport.effects;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import engine.Engine;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import graphicsSupport.texture.Texture;

@MonoDemeanor
public final class DirectionalLight {

	@MonoDemeanorInstance
	public static final DirectionalLight instance = new DirectionalLight();

	// Parameters
	private final Vector3f color = new Vector3f();
	private final Vector3f direction = new Vector3f();
	private float intensity;

	private ShadowMap shadowMap;
	private Texture[] shadowMaps = new Texture[Engine.getEngineOptions()
			.getDirectionalLightPerspectiveSplitsQuantity()];

	private DirectionalLight() {
	}

	@MonoDemeanorInit
	public void init() throws Exception {
		Vector2i[] ress = Engine.getEngineOptions().getDirectional_light_splits_resolution();
		shadowMap = new ShadowMap(1024, 1024);

		shadowMaps[0] = shadowMap.getTexture();
		for (int i = 1; i < shadowMaps.length; i++) {
			shadowMaps[i] = new Texture(ress[i].x, ress[i].y, GL11.GL_DEPTH_COMPONENT);
		}
	}

	public Vector3f getColor() {
		return color;
	}

	public Vector3f getDirection() {
		return direction;
	}

	public float getIntensity() {
		return intensity;
	}

	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	public void setDirection(float x, float y, float z) {
		direction.set(x, y, z);
		direction.normalize();
	}

	public void setColor(float x, float y, float z) {
		color.set(x, y, z);
	}

	public final ShadowMap getShadowMap() {
		return shadowMap;
	}

	/**
	 * @param direction2
	 */
	public void setDirection(Vector3f direction2) {
		direction.set(direction2);
		direction.normalize();
	}

	/**
	 * @param color2
	 */
	public void setColor(Vector3f color2) {
		color.set(color2);
	}

	public final Texture[] getShadowMaps() {
		return shadowMaps;
	}

}