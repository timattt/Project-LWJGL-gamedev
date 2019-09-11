package graphicsSupport;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import engine.Engine;
import engine.Window;
import engine.WindowMatricesManager;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import graphicsSupport.ExternalRenderer.RenderPriority;
import graphicsSupport.camera.Camera;
import graphicsSupport.effects.DirectionalLight;
import graphicsSupport.effects.DirectionalLightMatricesHandler;
import graphicsSupport.effects.PointLight;
import graphicsSupport.effects.ShadowMap;
import graphicsSupport.effects.SpotLight;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.particles.ParticlesGenerator;
import graphicsSupport.shaders.ShaderProgram;
import graphicsSupport.shaders.UniformedShaderProgram;
import graphicsSupport.texture.Texture;

@MonoDemeanor
public class Renderer {

	@MonoDemeanorInstance
	public static final Renderer instance = new Renderer();

	// Local instances
	private Universe universe;
	private Camera camera;

	private Renderer() {
	}

	public void render(Universe universe, Camera camera) {
		synchronized (universe) {
			this.universe = universe;
			this.camera = camera;

			HashMap<Integer, LinkedList<Item>> objects = universe.getObjects();
			LinkedList<PointLight> pointLights = universe.getPointLights();
			LinkedList<SpotLight> spotLights = universe.getSpotLights();
			LinkedList<ParticlesGenerator> generators = universe.getParticleGenerators();

			FrustumFilter.instance.recalculatePlanes(camera.getViewMatrix(),
					WindowMatricesManager.instance.getProjectionMatrix());

			Vector3f ambientLight = universe.getAmbientLight();
			DirectionalLight directionalLight = DirectionalLight.instance;

			/*
			 * Depth
			 */
			if (Engine.getEngineOptions().isEnabled_shadows()) {
				renderDirectionalLightDepthMap();
			}

			GL11.glViewport(0, 0, Window.instance.getWIDTH(), Window.instance.getHEIGHT());

			/*
			 * Sky box
			 */
			if (universe.getSkybox() != null) {
				drawSkybox();
			}

			/*
			 * External normal priority
			 */
			drawExternal_priority_normal(universe.getExternal_meshes(), directionalLight, pointLights, spotLights,
					ambientLight);
			/*
			 * Screen objects
			 */
			drawScreenObjects(objects, directionalLight, pointLights, spotLights, ambientLight);

			/*
			 * Particles
			 */
			drawParticles(directionalLight, pointLights, spotLights, ambientLight, generators);
			/*
			 * External high priority
			 */
			drawExternal_priority_hight(universe.getExternal_meshes(), directionalLight, pointLights, spotLights,
					ambientLight);
			/*
			 * GUI
			 */
			drawGUI();

			this.universe = null;
			this.camera = null;
		}
	}

	private void drawGUI() {
		VgGuiHandler.instance.render();
	}

	private void drawSkybox() {
		ShaderProgram.skyboxShader.bind();
		ShaderProgram.skyboxShader.setUniform("projectionMatrix", WindowMatricesManager.instance.getProjectionMatrix());
		ShaderProgram.skyboxShader.setUniform("viewMatrix", camera.getViewMatrix());

		GL11.glDepthMask(false);
		universe.getSkybox().getBaseMesh().drawTexturedSkybox(universe.getSkybox().getNightCoefficient());
		GL11.glDepthMask(true);

		ShaderProgram.skyboxShader.unbind();
	}

	private void drawExternal_priority_normal(LinkedList<ExternalRenderer> meshes, DirectionalLight directionalLight,
			LinkedList<PointLight> pointLights, LinkedList<SpotLight> spotLights, Vector3f ambientLight) {
		for (ExternalRenderer mesh : meshes) {
			if (mesh.getRenderPriority() == RenderPriority.normal && mesh.mustBeRendered()) {
				UniformedShaderProgram shader = mesh.getShader();

				mesh.init();

				if (mesh.hasShader()) {
					shader.bind();

					if (mesh.needLightSetup()) {
						setEffects(shader);
						setUpLight(shader, directionalLight, pointLights, spotLights, ambientLight);

						shader.setUniform("frustum", FrustumFilter.instance.getPlanes());
						shader.setUniform("projectionMatrix", WindowMatricesManager.instance.getProjectionMatrix());
						shader.setUniform("viewMatrix", camera.getViewMatrix());
					}
				}

				mesh.render(camera);

				if (mesh.hasShader()) {
					shader.unbind();
				}

				mesh.end();
			}
		}
	}

	private void drawExternal_priority_hight(LinkedList<ExternalRenderer> meshes, DirectionalLight directionalLight,
			LinkedList<PointLight> pointLights, LinkedList<SpotLight> spotLights, Vector3f ambientLight) {
		for (ExternalRenderer mesh : meshes) {
			if (mesh.getRenderPriority() == RenderPriority.hight && mesh.mustBeRendered()) {
				UniformedShaderProgram shader = mesh.getShader();

				shader.bind();

				setEffects(shader);
				setUpLight(shader, directionalLight, pointLights, spotLights, ambientLight);

				shader.setUniform("frustum", FrustumFilter.instance.getPlanes());
				shader.setUniform("projectionMatrix", WindowMatricesManager.instance.getProjectionMatrix());
				shader.setUniform("viewMatrix", camera.getViewMatrix());

				mesh.render(camera);

				shader.unbind();
			}
		}
	}

	private void drawScreenObjects(HashMap<Integer, LinkedList<Item>> objects, DirectionalLight directionalLight,
			LinkedList<PointLight> pointLights, LinkedList<SpotLight> spotLights, Vector3f ambientLight) {
		renderChunkedObjects(directionalLight, pointLights, spotLights, ambientLight, objects);
		renderNonChunkedObjects(directionalLight, pointLights, spotLights, ambientLight, objects);
	}

	private void drawParticles(DirectionalLight directionalLight, LinkedList<PointLight> pointLights,
			LinkedList<SpotLight> spotLights, Vector3f ambientLight, LinkedList<ParticlesGenerator> particleGens) {

		ShaderProgram.particleShader.bind();

		// Projection and view matrixes preparation
		ShaderProgram.particleShader.setUniform("projectionMatrix",
				WindowMatricesManager.instance.getProjectionMatrix());
		ShaderProgram.particleShader.setUniform("viewMatrix", camera.getViewMatrix());
		GL11.glDepthMask(false);

		for (ParticlesGenerator gen : particleGens) {
			gen.update();
			if (gen.getParticles().size() == 0) {
				continue;
			}
			for (Mesh mesh : gen.getParticles().getFirst().getMeshes()) {
				mesh.renderParticlesList(gen.getParticles(), gen.getTexture(), camera);
			}
		}

		GL11.glDepthMask(true);

		ShaderProgram.particleShader.unbind();
	}

	protected void setUpLight(UniformedShaderProgram shader, DirectionalLight directionalLight,
			LinkedList<PointLight> pointLights, LinkedList<SpotLight> spotLights, Vector3f ambientLight) {

		shader.setUniform("ambientLight", ambientLight);

		shader.setUniform("specularPower", universe.getSpecularPower());

		// Get a copy of the directional light object and transform its position
		// to view coordinates
		Vector4f dir = new Vector4f(directionalLight.getDirection(), 0);
		Vector3f oldDir = new Vector3f(directionalLight.getDirection());
		dir.mul(camera.getViewMatrix());
		directionalLight.setDirection(dir.x, dir.y, dir.z);
		shader.setUniform("directionalLight", directionalLight);
		directionalLight.setDirection(oldDir.x, oldDir.y, oldDir.y);

		// Get a copy of the light object and transform its position to view
		// coordinates
		PointLight[] currPointLights = new PointLight[pointLights.size()];

		for (int i = 0; i < pointLights.size(); i++) {
			PointLight currPointLight = new PointLight(pointLights.get(i));
			Vector3f lightPos = currPointLight.getPosition();
			Vector4f aux = new Vector4f(lightPos, 1);
			aux.mul(camera.getViewMatrix());
			lightPos.x = aux.x;
			lightPos.y = aux.y;
			lightPos.z = aux.z;
			currPointLights[i] = currPointLight;
		}
		shader.setUniform("pointLights", currPointLights);

		// Get a copy of the spot light object and transform its position and
		// cone direction to view coordinates
		SpotLight[] currSpotLights = new SpotLight[spotLights.size()];

		for (int i = 0; i < spotLights.size(); i++) {
			SpotLight currSpotLight = new SpotLight(spotLights.get(i));
			dir = new Vector4f(currSpotLight.getConeDirection(), 0);
			dir.mul(camera.getViewMatrix());
			currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));

			Vector3f spotLightPos = currSpotLight.getPointLight().getPosition();
			Vector4f auxSpot = new Vector4f(spotLightPos, 1);
			auxSpot.mul(camera.getViewMatrix());
			spotLightPos.x = auxSpot.x;
			spotLightPos.y = auxSpot.y;
			spotLightPos.z = auxSpot.z;

			currSpotLights[i] = currSpotLight;
		}

		shader.setUniform("spotLights", currSpotLights);
	}

	protected void setEffects(UniformedShaderProgram shader) {
		// Fog
		shader.setUniform("fog", universe.getFog(), universe.hasFog());
	}

	private final void renderNonChunkedObjects(DirectionalLight directionalLight, LinkedList<PointLight> pointLights,
			LinkedList<SpotLight> spotLights, Vector3f ambientLight, HashMap<Integer, LinkedList<Item>> objects) {

		// Binding
		ShaderProgram.nonChunkedShader.bind();

		// Frustum
		ShaderProgram.nonChunkedShader.setUniform("frustum", FrustumFilter.instance.getPlanes());

		// Setting effects
		setEffects(ShaderProgram.nonChunkedShader);

		// Lightning
		setUpLight(ShaderProgram.nonChunkedShader, directionalLight, pointLights, spotLights, ambientLight);

		// Matrices
		ShaderProgram.nonChunkedShader.setUniform("projectionMatrix",
				WindowMatricesManager.instance.getProjectionMatrix());
		ShaderProgram.nonChunkedShader.setUniform("viewMatrix", camera.getViewMatrix());

		synchronized (objects) {

			for (int key : objects.keySet()) {
				if (objects.get(key).size() == 0 || !objects.get(key).getFirst().hasMeshes()
						|| objects.get(key).getFirst().getBaseMesh().getType() != Mesh.MESH) {
					continue;
				}

				for (Mesh mesh : objects.get(key).getFirst().getMeshes()) {
					synchronized (objects.get(key)) {
						mesh.renderObjectsList(objects.get(key), camera);
					}
				}
			}

		}

		ShaderProgram.nonChunkedShader.unbind();

	}

	private final void renderChunkedObjects(DirectionalLight directionalLight, LinkedList<PointLight> pointLights,
			LinkedList<SpotLight> spotLights, Vector3f ambientLight, HashMap<Integer, LinkedList<Item>> objects) {

		// Binding
		ShaderProgram.chunkedShader.bind();

		// Frustum
		ShaderProgram.chunkedShader.setUniform("frustum", FrustumFilter.instance.getPlanes());

		// Setting effects
		setEffects(ShaderProgram.chunkedShader);

		// Lightning
		setUpLight(ShaderProgram.chunkedShader, directionalLight, pointLights, spotLights, ambientLight);

		// Matrices
		ShaderProgram.chunkedShader.setUniform("projectionMatrix",
				WindowMatricesManager.instance.getProjectionMatrix());
		ShaderProgram.chunkedShader.setUniform("viewMatrix", camera.getViewMatrix());

		synchronized (objects.keySet()) {

			for (int key : objects.keySet()) {
				if (objects.get(key).size() == 0 || !objects.get(key).getFirst().hasMeshes()
						|| objects.get(key).getFirst().getBaseMesh().getType() != Mesh.CHUNKED_MESH) {
					continue;
				}
				for (Mesh mesh : objects.get(key).getFirst().getMeshes()) {
					synchronized (objects.get(key)) {
						mesh.renderObjectsList(objects.get(key), camera);
					}
				}
			}
			ShaderProgram.chunkedShader.unbind();

		}
	}

	private void renderDirectionalLightDepthMap() {
		DirectionalLight light = DirectionalLight.instance;
		ShadowMap shadowMap = light.getShadowMap();
		Texture[] shadowMaps = light.getShadowMaps();

		DirectionalLightMatricesHandler.instance.updateMatrices(camera);

		shadowMap.bind();
		ShaderProgram.depthShader.bind();

		for (int i = 0; i < shadowMaps.length; i++) {
			GL11.glViewport(0, 0, shadowMaps[i].getWidth(), shadowMaps[i].getHeight());
			shadowMap.setTexture(shadowMaps[i]);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

			ShaderProgram.depthShader.setUniform("viewMatrix",
					DirectionalLightMatricesHandler.instance.getLightViewMatrix(i));
			ShaderProgram.depthShader.setUniform("projectionMatrix",
					DirectionalLightMatricesHandler.instance.getOrthoProjectionMatrix(i));

			HashMap<Integer, LinkedList<Item>> objects = universe.getObjects();

			synchronized (objects) {
				ShaderProgram.depthShader.setUniform("chunked", 0f);
				for (int key : objects.keySet()) {
					if (objects.get(key).size() == 0 || !objects.get(key).getFirst().hasMeshes()
							|| objects.get(key).getFirst().getBaseMesh().getType() != Mesh.MESH) {
						continue;
					}

					for (Mesh mesh : objects.get(key).getFirst().getMeshes()) {
						synchronized (objects.get(key)) {
							mesh.renderDepth(objects.get(key), camera, i);
						}
					}
				}
				ShaderProgram.depthShader.setUniform("chunked", 1f);

				for (int key : objects.keySet()) {
					if (objects.get(key).size() == 0 || !objects.get(key).getFirst().hasMeshes()
							|| objects.get(key).getFirst().getBaseMesh().getType() != Mesh.CHUNKED_MESH) {
						continue;
					}
					for (Mesh mesh : objects.get(key).getFirst().getMeshes()) {
						synchronized (objects.get(key)) {
							mesh.renderDepth(objects.get(key), camera, i);
						}
					}
				}
			}
		}

		ShaderProgram.depthShader.unbind();

		shadowMap.unbind();

	}

}
