package graphicsSupport.shaders;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Scanner;

import engine.Engine;

public class ShaderProgram {

	// Shaders
	public static final LinkedList<ShaderProgram> ALL_SHADERS = new LinkedList<ShaderProgram>();

	public static final UniformedShaderProgram chunkedShader = UniformedShaderProgram.createUniformedShaderProgram(
			"/engine/shaders/chunked/ChunkedVertexShader.vert", "/engine/shaders/chunked/ChunkedFragmentShader.frag",
			new String[] {
					// Matrices
					"projectionMatrix", "viewMatrix",

					// Lights
					"ambientLight", "pointLights", "directionalLight", "spotLights",

					// Material
					"material", "specularPower",

					// Effects
					"fog",

					// Frustum
					"frustum" });

	public static final UniformedShaderProgram nonChunkedShader = UniformedShaderProgram.createUniformedShaderProgram(
			"/engine/shaders/nonChunked/NonChunkedVertexShader.vert",
			"/engine/shaders/nonChunked/NonChunkedFragmentShader.frag", new String[] {
					// Matrices
					"projectionMatrix", "viewMatrix", "modelMatrix",

					// Lights
					"ambientLight", "pointLights", "directionalLight", "spotLights",

					// Material
					"material", "specularPower",

					// Effects
					"fog",

					// Frustum
					"frustum" });

	public static final UniformedShaderProgram particleShader = UniformedShaderProgram.createUniformedShaderProgram(
			"/engine/shaders/particle/VertexParticleShader.vert",
			"/engine/shaders/particle/FragmentParticleShader.frag",
			new String[] { "projectionMatrix", "modelMatrix", "viewMatrix", "frag_texture", "alpha" });

	public static final UniformedShaderProgram skyboxShader = UniformedShaderProgram.createUniformedShaderProgram(
			"/engine/shaders/skybox/SkyboxVertexShader.vert", "/engine/shaders/skybox/SkyboxFragmentShader.frag",
			new String[] { "projectionMatrix", "frag_texture", "viewMatrix", "night_coef" });

	public static final UniformedShaderProgram depthShader = UniformedShaderProgram.createUniformedShaderProgram(
			"/engine/shaders/depth/DepthVertexShader.vert", "/engine/shaders/depth/DepthFragmentShader.frag",
			new String[] { "modelMatrix", "projectionMatrix", "viewMatrix", "chunked" });

	protected final int programId;

	private int vertexShaderId;

	private int fragmentShaderId;

	protected boolean external = false;

	public ShaderProgram() throws Exception {
		programId = glCreateProgram();
		if (programId == 0) {
			throw new Exception("Could not create Shader");
		}
	}

	protected final void externalize() {
		external = true;
	}

	public void createVertexShader(String shaderCode) throws Exception {
		vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
	}

	public void createFragmentShader(String shaderCode) throws Exception {
		fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
	}

	protected int createShader(String shaderCode, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if (shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}

		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}

		glAttachShader(programId, shaderId);

		return shaderId;
	}

	public void link() throws Exception {
		glLinkProgram(programId);
		if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
		}

		if (vertexShaderId != 0) {
			glDetachShader(programId, vertexShaderId);
		}
		if (fragmentShaderId != 0) {
			glDetachShader(programId, fragmentShaderId);
		}

		glValidateProgram(programId);
		if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
		}

	}

	public void bind() {
		glUseProgram(programId);
	}

	public void unbind() {
		glUseProgram(0);
	}

	public void cleanup() {
		unbind();
		if (programId != 0) {
			glDeleteProgram(programId);
		}
		ALL_SHADERS.remove(this);
	}

	public static ShaderProgram createShaderProgram(String verts, String frags) {
		try {
			ShaderProgram shaderProg = new ShaderProgram();
			shaderProg.createVertexShader(loadShader(verts));
			shaderProg.createFragmentShader(loadShader(frags));
			shaderProg.link();
			if (!ALL_SHADERS.contains(shaderProg)) {
				ALL_SHADERS.add(shaderProg);
			}
			return shaderProg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static String loadShader(String fileName) throws Exception {
		String result;
		try (InputStream in = Engine.getLibraryResourceAsStream(fileName); Scanner scanner = new Scanner(in, "UTF-8")) {
			result = scanner.useDelimiter("\\A").next();
		}

		return result;
	}

	protected static String loadShader(File fileName) throws Exception {
		String result;
		try (InputStream in = new FileInputStream(fileName); Scanner scanner = new Scanner(in, "UTF-8")) {
			result = scanner.useDelimiter("\\A").next();
		}

		return result;
	}

	public static void masscleanup() {
		while (!ALL_SHADERS.isEmpty()) {
			ALL_SHADERS.getFirst().cleanup();
		}
	}

}