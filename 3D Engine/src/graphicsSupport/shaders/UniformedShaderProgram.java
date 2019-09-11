package graphicsSupport.shaders;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import graphicsSupport.effects.DirectionalLight;
import graphicsSupport.effects.Fog;
import graphicsSupport.effects.Material;
import graphicsSupport.effects.PointLight;
import graphicsSupport.effects.SpotLight;
import graphicsSupport.texture.Texture;
import utilities.Console;

public class UniformedShaderProgram extends ShaderProgram {

	// Constants
	public static final int maxLights = 10;

	// Uniforms
	private final Map<String, Integer> uniforms;
	private final LinkedList<String> nullUniforms = new LinkedList<String>();

	public UniformedShaderProgram() throws Exception {
		uniforms = new HashMap<>();
	}

	public void createUniform(String uniformName) throws Exception {
		int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
		if (uniformLocation < 0) {
			nullUniforms.add(uniformName);
			return;
		}
		uniforms.put(uniformName, uniformLocation);
	}

	public void createTextureUniform(String uniformName) throws Exception {
		createUniform(uniformName + ".texture_sampler");
		createUniform(uniformName + ".texXOffset");
		createUniform(uniformName + ".texYOffset");
		createUniform(uniformName + ".numCols");
		createUniform(uniformName + ".numRows");
	}

	public void createPointLightUniform(String uniformName) throws Exception {
		createUniform(uniformName + ".colour");
		createUniform(uniformName + ".position");
		createUniform(uniformName + ".intensity");
		createUniform(uniformName + ".att.constant");
		createUniform(uniformName + ".att.linear");
		createUniform(uniformName + ".att.exponent");
	}

	public void createPointLightsListUniform(String uniformName, int size) {
		for (int i = 0; i < size; i++) {
			try {
				createPointLightUniform(uniformName + "[" + i + "]");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createListUniform(String uniformName, int size) {
		for (int i = 0; i < size; i++) {
			try {
				createUniform(uniformName + "[" + i + "]");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createSpotLightsListUniform(String uniformName, int size) {
		for (int i = 0; i < size; i++) {
			try {
				createSpotLightUniform(uniformName + "[" + i + "]");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createMaterialUniform(String uniformName) throws Exception {
		createUniform(uniformName + ".transparency");
		createUniform(uniformName + ".ambientColor");
		createUniform(uniformName + ".diffuseColor");
		createUniform(uniformName + ".specularColor");
		createUniform(uniformName + ".reflectance");
		createTextureUniform(uniformName + ".diffuse_map");
		createUniform(uniformName + ".has_diffuse_map");
	}

	public void createDirectionalLightUniform(String uniformName) throws Exception {
		createUniform(uniformName + ".colour");
		createUniform(uniformName + ".direction");
		createUniform(uniformName + ".intensity");
	}

	public void createSpotLightUniform(String uniformName) throws Exception {
		createPointLightUniform(uniformName + ".pl");
		createUniform(uniformName + ".conedir");
		createUniform(uniformName + ".cutoff");
	}

	public void createFrustumUniform(String uniformName) throws Exception {
		for (int i = 0; i < 6; i++) {
			createUniform(uniformName + "[" + i + "]");
		}
	}

	public void createFogUniform(String uniform) throws Exception {
		createUniform(uniform + "._active");
		createUniform(uniform + ".color");
		createUniform(uniform + ".exponent");
		createUniform(uniform + ".density");
	}

	public void setUniform(String uniformName, Matrix4f matrix) {
		if (nullUniforms.contains(uniformName)) {
			return;
		}
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			matrix.get(fb);
			GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}

	public void setUniform(String uniformName, Matrix4f[] matrices) {
		if (nullUniforms.contains(uniformName)) {
			return;
		}
		try (MemoryStack stack = MemoryStack.stackPush()) {
			int length = matrices != null ? matrices.length : 0;
			FloatBuffer fb = stack.mallocFloat(16 * length);
			for (int i = 0; i < length; i++) {
				matrices[i].get(16 * i, fb);
			}
			GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}

	public void setUniform(String uniformName, Texture texture) {
		setUniform(uniformName + ".texture_sampler", 0);
		Vector2f result = texture.getFrameOfsets();
		setUniform(uniformName + ".texXOffset", result.x);
		setUniform(uniformName + ".texYOffset", result.y);
		setUniform(uniformName + ".numCols", texture.getNum_cols());
		setUniform(uniformName + ".numRows", texture.getNum_rows());
	}

	public void setUniform(String uniformName, Fog fog, boolean active) {
		setUniform(uniformName + "._active", (active ? 1 : 0));
		if (fog == null) {
			return;
		}
		setUniform(uniformName + ".color", fog.getColor());
		setUniform(uniformName + ".exponent", fog.getExponent());
		setUniform(uniformName + ".density", fog.getDensity());
	}

	public void setUniform(String uniformName, PointLight[] pls) {
		for (int i = 0; i < (pls != null ? pls.length : 0); i++) {
			setUniform(uniformName + "[" + i + "]", pls[i]);
		}
	}

	public void setUniform(String uniformName, SpotLight[] sls) {
		for (int i = 0; i < (sls != null ? sls.length : 0); i++) {
			setUniform(uniformName + "[" + i + "]", sls[i]);
		}
	}

	public void setUniform(String uniformName, float value) {
		if (nullUniforms.contains(uniformName)) {
			return;
		}
		GL20.glUniform1f(uniforms.get(uniformName), value);
	}

	public void setUniform(String uniformName, SpotLight spotLight) {
		setUniform(uniformName + ".pl", spotLight.getPointLight());
		setUniform(uniformName + ".conedir", spotLight.getConeDirection());
		setUniform(uniformName + ".cutoff", spotLight.getCutOff());
	}

	public void setUniform(String uniformName, Vector3f value) {
		if (nullUniforms.contains(uniformName)) {
			return;
		}
		GL20.glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
	}

	public void setUniform(String uniformName, Vector4f value) {
		if (nullUniforms.contains(uniformName)) {
			return;
		}
		GL20.glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
	}

	public void setUniform(String uniformName, Vector4f[] value) {
		for (int i = 0; i < value.length; i++) {
			if (value[i] != null) {
				setUniform(uniformName + "[" + i + "]", value[i]);
			}
		}
	}

	public void setUniform(String uniformName, float[] value) {
		for (int i = 0; i < value.length; i++) {
			setUniform(uniformName + "[" + i + "]", value[i]);
		}
	}

	public void setUniform(String uniformName, DirectionalLight dirLight) {
		setUniform(uniformName + ".colour", dirLight.getColor());
		setUniform(uniformName + ".direction", dirLight.getDirection());
		setUniform(uniformName + ".intensity", dirLight.getIntensity());
	}

	public void setUniform(String uniformName, PointLight pointLight) {
		setUniform(uniformName + ".colour", pointLight.getColor());
		setUniform(uniformName + ".position", pointLight.getPosition());
		setUniform(uniformName + ".intensity", pointLight.getIntensity());
		PointLight.Attenuation att = pointLight.getAttenuation();
		setUniform(uniformName + ".att.constant", att.getConstant());
		setUniform(uniformName + ".att.linear", att.getLinear());
		setUniform(uniformName + ".att.exponent", att.getExponent());
	}

	public void setUniform(String uniformName, Material material) {
		setUniform(uniformName + ".transparency", material.getTransparency());
		setUniform(uniformName + ".ambientColor", material.getAmbientColor());
		setUniform(uniformName + ".diffuseColor", material.getDiffuseColor());
		setUniform(uniformName + ".specularColor", material.getSpecularColor());
		setUniform(uniformName + ".reflectance", material.getReflectance());

		if (material.hasDiffuseTexture()) {
			setUniform(uniformName + ".diffuse_map", material.getTexture());
			setUniform(uniformName + ".has_diffuse_map", 1f);
		} else {
			setUniform(uniformName + ".has_diffuse_map", 0f);
		}

	}

	public static UniformedShaderProgram createUniformedShaderProgram(String verts, String frags, String[] uniforms) {
		try {
			UniformedShaderProgram unProg = new UniformedShaderProgram();
			unProg.createVertexShader(loadShader(verts));
			unProg.createFragmentShader(loadShader(frags));
			unProg.link();
			for (String unif : uniforms) {
				if (unif == "material") {
					unProg.createMaterialUniform(unif);
					continue;
				}
				if (unif == "pointLights") {
					unProg.createPointLightsListUniform(unif, maxLights);
					continue;
				}
				if (unif == "directionalLight") {
					unProg.createDirectionalLightUniform(unif);
					continue;
				}
				if (unif == "spotLights") {
					unProg.createSpotLightsListUniform(unif, maxLights);
					continue;
				}
				if (unif == "fog") {
					unProg.createFogUniform(unif);
					continue;
				}
				if (unif == "frag_texture") {
					unProg.createTextureUniform(unif);
					continue;
				}
				if (unif == "frustum") {
					unProg.createFrustumUniform(unif);
					continue;
				}
				unProg.createUniform(unif);
			}

			if (!unProg.nullUniforms.isEmpty()) {
				Console.println_err("Warning! Some uniforms {" + unProg.nullUniforms.toString() + "} are null!");
			}

			if (!ALL_SHADERS.contains(unProg)) {
				ALL_SHADERS.add(unProg);
			}
			return unProg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static UniformedShaderProgram createExternalUniformedShaderProgram(File verts, File frags,
			String[] uniforms) {
		try {
			UniformedShaderProgram unProg = new UniformedShaderProgram();
			unProg.createVertexShader(loadShader(verts));
			unProg.createFragmentShader(loadShader(frags));
			unProg.link();
			
			unProg.externalize();
			
			for (String unif : uniforms) {
				if (unif == "material") {
					unProg.createMaterialUniform(unif);
					continue;
				}
				if (unif == "pointLights") {
					unProg.createPointLightsListUniform(unif, maxLights);
					continue;
				}
				if (unif == "directionalLight") {
					unProg.createDirectionalLightUniform(unif);
					continue;
				}
				if (unif == "spotLights") {
					unProg.createSpotLightsListUniform(unif, maxLights);
					continue;
				}
				if (unif == "fog") {
					unProg.createFogUniform(unif);
					continue;
				}
				if (unif == "frag_texture") {
					unProg.createTextureUniform(unif);
					continue;
				}
				if (unif == "frustum") {
					unProg.createFrustumUniform(unif);
					continue;
				}
				unProg.createUniform(unif);
			}

			if (!unProg.nullUniforms.isEmpty()) {
				Console.println_err("Warning! Some uniforms {" + unProg.nullUniforms.toString() + "} are null!");
			}

			if (!ALL_SHADERS.contains(unProg)) {
				ALL_SHADERS.add(unProg);
			}
			return unProg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setUniform(String uniformName, int value) {
		if (nullUniforms.contains(uniformName)) {
			return;
		}
		GL20.glUniform1i(uniforms.get(uniformName), value);
	}

}
