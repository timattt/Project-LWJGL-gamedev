package graphicsSupport;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector3f;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import graphicsSupport.effects.DirectionalLight;
import graphicsSupport.effects.Fog;
import graphicsSupport.effects.PointLight;
import graphicsSupport.effects.SkyBox;
import graphicsSupport.effects.SpotLight;
import graphicsSupport.particles.ParticlesGenerator;

@MonoDemeanor()
public class Universe {

	@MonoDemeanorInstance
	public static final Universe instance = new Universe();

	// Screen objects
	private LinkedList<ExternalRenderer> external_renderers = new LinkedList<ExternalRenderer>();

	// Light
	private final Vector3f ambientLight;
	
	// Particles
	private final LinkedList<ParticlesGenerator> particleGenerators = new LinkedList<ParticlesGenerator>();

	// Screen objects
	private final HashMap<Integer, LinkedList<Item>> objects = new HashMap<Integer, LinkedList<Item>>();

	// Lights
	private final LinkedList<PointLight> pointLights = new LinkedList<PointLight>();
	private final LinkedList<SpotLight> spotLights = new LinkedList<SpotLight>();

	// Effects
	private Fog fog;
	private float specularPower = 10f;

	// Skybox
	protected SkyBox skybox;

	private Universe() {
		ambientLight = new Vector3f();
		DirectionalLight.instance.setDirection(1f, 1f, 0f);
		DirectionalLight.instance.setColor(1f, 1f, 1f);
		DirectionalLight.instance.setIntensity(1f);
	}

	public final void setFog(Fog fog) {
		this.fog = fog;
	}

	public boolean hasFog() {
		return fog != null;
	}

	public void update(Renderer rend) {
		for (ParticlesGenerator gen : particleGenerators) {
			gen.update();
		}
	}

	public void setAmbientLight(Vector3f light) {
		ambientLight.set(light);
	}

	public final Fog getFog() {
		return fog;
	}

	public final Vector3f getAmbientLight() {
		return ambientLight;
	}

	public final void addPointLight(PointLight pl) {
		pointLights.add(pl);
	}

	public final void addSpotLight(SpotLight sl) {
		spotLights.addLast(sl);
	}

	public final boolean contains(Item obj) {
		return objects.containsKey(obj.getBaseMesh().ID) && objects.get(obj.getBaseMesh().ID).contains(obj);
	}

	public final void addObject(Item obj) {
		synchronized (objects) {
			if (!objects.containsKey(obj.getMeshes()[0].ID)) {
				synchronized (objects.keySet()) {
					objects.put(obj.getMeshes()[0].ID, new LinkedList<Item>());
				}
			}
			LinkedList<Item> objs = objects.get(obj.getMeshes()[0].ID);
			synchronized (objs) {
				objs.add(obj);
			}
		}
	}

	public final void deleteObject(Item obj) {
		synchronized (objects) {
			if (!objects.containsKey(obj.getMeshes()[0].ID)) {
				return;
			}

			LinkedList<Item> objs = objects.get(obj.getMeshes()[0].ID);

			synchronized (objs) {
				objs.remove(obj);
				if (objects.get(obj.getMeshes()[0].ID).isEmpty()) {
					synchronized (objects.keySet()) {
						objects.remove(obj.getMeshes()[0].ID);

					}
				}
			}
		}
	}

	public int getObjectQuantity() {
		int quant = 0;
		for (int key : objects.keySet()) {
			quant = objects.get(key).size() + quant;
		}
		return quant;
	}

	public final void addParticleGenerator(ParticlesGenerator gen) {
		particleGenerators.add(gen);
	}

	public final void addExternalMesh(ExternalRenderer mesh) {
		external_renderers.add(mesh);
	}

	public final void removeExternalMesh(ExternalRenderer mesh) {
		external_renderers.remove(mesh);
	}

	public final LinkedList<ExternalRenderer> getExternal_meshes() {
		return external_renderers;
	}

	public final SkyBox getSkybox() {
		return skybox;
	}

	public final void setSkybox(SkyBox skybox) {
		this.skybox = skybox;
	}

	public final LinkedList<ParticlesGenerator> getParticleGenerators() {
		return particleGenerators;
	}

	public final HashMap<Integer, LinkedList<Item>> getObjects() {
		return objects;
	}

	public final LinkedList<PointLight> getPointLights() {
		return pointLights;
	}

	public final LinkedList<SpotLight> getSpotLights() {
		return spotLights;
	}

	public final float getSpecularPower() {
		return specularPower;
	}

	public final void setSpecularPower(float specularPower) {
		this.specularPower = specularPower;
	}
}
