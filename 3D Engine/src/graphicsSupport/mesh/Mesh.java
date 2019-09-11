package graphicsSupport.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import graphicsSupport.Item;
import graphicsSupport.camera.Camera;
import graphicsSupport.effects.DirectionalLightMatricesHandler;
import graphicsSupport.effects.Material;
import graphicsSupport.shaders.ShaderProgram;
import graphicsSupport.texture.Texture;
import resources.Resource;

public class Mesh implements Resource {

	// Meshes
	public static final HashMap<Integer, Mesh[]> ALL_LOADED_MESHES = new HashMap<Integer, Mesh[]>();

	// Name
	public final String NAME;
	public String AUTHOR;

	// Material
	public final Material material;

	// Shape ID
	public final int ID;

	// Wireframe
	private boolean renderWireFrame = false;

	// VAO and VBO
	public final int vaoId;

	public final LinkedList<Integer> vbos = new LinkedList<Integer>();

	protected int TYPE;
	protected boolean hasUV;

	public final static int MESH = 1;
	public final static int CHUNKED_MESH = 2;
	public final static int ANIMATION = 3;

	public static final int MAX_WEIGHTS = 4;

	// Vertices
	public final int vertexQuantity;

	// Size
	public final Vector3f size;
	public final float radius;

	/**
	 * Empty color
	 * 
	 * @param verts
	 * @param normals
	 * @param inds
	 * @param mat
	 */
	public Mesh(float[] verts, float[] normals, int[] inds, String name, Material mat, int id) {
		material = mat;
		NAME = name;
		TYPE = 1;
		vertexQuantity = inds.length;

		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		vbos.add(this.loadVertices(verts, 0)); // Vertexes at position 0;
		vbos.add(this.loadNormals(normals, 1)); // Normals at position 1;
		vbos.add(this.loadIndices(inds));

		GL30.glBindVertexArray(0);
		
		// Creating key
		if (id == -1) {
			ID = createKey();
			ALL_LOADED_MESHES.put(ID, new Mesh[] { this });
		} else {
			ID = id;
		}

		size = new Vector3f();
		calcSize(verts);
		radius = size.length();
	}

	/**
	 * Textured
	 * 
	 * @param verts
	 * @param normals
	 * @param textPos
	 * @param inds
	 * @param text
	 * @param mat
	 */
	public Mesh(float[] verts, float[] normals, float[] textPos, int[] inds, String name, Material mat, int id) {
		material = mat;
		NAME = name;
		TYPE = 1;
		vertexQuantity = inds.length;

		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		vbos.add(this.loadVertices(convert_arr_to_indices(verts, 3, inds), 0)); // Vertexes
																				// at
																				// position
																				// 0;
		vbos.add(this.loadNormals(convert_arr_to_indices(normals, 3, inds), 1)); // Normals
																					// at
																					// position
																					// 1;
		vbos.add(this.loadTexturePositions(convert_arr_to_indices(textPos, 2, inds), 3));// Texture
																							// POS
																							// at
		// position 3;
		// vbos.add(this.loadIndices(inds));

		GL30.glBindVertexArray(0);

		// Creating key
		if (id == -1) {
			ID = createKey();
			ALL_LOADED_MESHES.put(ID, new Mesh[] { this });
		} else {
			ID = id;
		}

		hasUV = true;
		size = new Vector3f();
		calcSize(verts);
		radius = size.length();
	}
	
	protected int getStoreMode() {
		return GL15.GL_STATIC_DRAW;
	}

	protected void calcSize(float[] arr) {
		for (int i = 0; i < arr.length; i += 3) {
			size.x = Math.max(Math.abs(arr[i]), size.x);
			size.y = Math.max(Math.abs(arr[i + 1]), size.y);
			size.z = Math.max(Math.abs(arr[i + 2]), size.z);
		}
	}

	protected static float[] convert_arr_to_indices(float[] arr, int scal, int[] inds) {
		float[] result = new float[inds.length * scal];

		for (int i = 0; i < inds.length; i++) {
			for (int a = 0; a < scal; a++) {
				result[i * scal + a] = arr[inds[i] * scal + a];
			}
		}

		return result;
	}

	public static final int createKey() {
		int newKey = 0;
		while (ALL_LOADED_MESHES.containsKey(newKey)) {
			newKey++;
		}
		return newKey;
	}

	public void renderObjectsList(LinkedList<Item> objs, Camera cam) {

		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, (material.hasDiffuseTexture() ? material.getTexture().getGLId() : 0));
		GL20.glEnableVertexAttribArray(3);

		ShaderProgram.nonChunkedShader.setUniform("material", material);

		for (Item obj : objs) {
			Matrix4f mat = obj.getModelMatrix();
			mat.m33(obj.getColorScalar());

			if (obj.isBillboard()) {
				cam.getViewMatrix().transpose3x3(mat);
			}

			ShaderProgram.nonChunkedShader.setUniform("modelMatrix", mat);
			mat.m33(1f);
			
			if (material.hasDiffuseTexture() || hasUV) {
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertexQuantity);
			} else {
				GL11.glDrawElements(GL11.GL_TRIANGLES, this.vertexQuantity, GL11.GL_UNSIGNED_INT, 0);
			}
		}

		GL20.glDisableVertexAttribArray(3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Disable normals VBO
		GL20.glDisableVertexAttribArray(1);

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);

	}
	
	public void renderDepth(LinkedList<Item> objs, Camera cam, int cascade) {

		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		for (Item obj : objs) {
			Matrix4f mat = obj.getModelMatrix();

			if (obj.isBillboard()) {
				cam.getViewMatrix().transpose3x3(mat);
			}

			if (!obj.checkInFrustum(DirectionalLightMatricesHandler.instance.getMatricesPlanes()[cascade])) {
				continue;
			}
			
			ShaderProgram.depthShader.setUniform("modelMatrix", mat);
			if (material.hasDiffuseTexture() || hasUV) {
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertexQuantity);
			} else {
				GL11.glDrawElements(GL11.GL_TRIANGLES, this.vertexQuantity, GL11.GL_UNSIGNED_INT, 0);
			}
		}

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);

	}

	public void renderParticlesList(LinkedList<Item> objs, Texture texture, Camera cam) {

		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, (texture.getGLId()));
		GL20.glEnableVertexAttribArray(3);

		ShaderProgram.particleShader.setUniform("frag_texture", texture);

		for (Item obj : objs) {
			Matrix4f mat = obj.getModelMatrix();

			if (obj.isBillboard()) {
				cam.getViewMatrix().transpose3x3(mat);
				mat.scale(obj.getScale());
			}

			ShaderProgram.particleShader.setUniform("modelMatrix", mat);
			ShaderProgram.particleShader.setUniform("alpha", obj.getAlpha());

			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertexQuantity);
		}

		GL20.glDisableVertexAttribArray(3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Disable normals VBO
		GL20.glDisableVertexAttribArray(1);

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);

	}

	public void drawTexturedSkybox(float coef) {
		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getTexture().getGLId());
		GL20.glEnableVertexAttribArray(3);

		ShaderProgram.skyboxShader.setUniform("frag_texture", material.getTexture());
		ShaderProgram.skyboxShader.setUniform("night_coef", coef);

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertexQuantity);

		GL20.glDisableVertexAttribArray(3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Disable normals VBO
		GL20.glDisableVertexAttribArray(1);

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	public int getType() {
		return TYPE;
	}

	@Override
	public void cleanup() {
		GL20.glDisableVertexAttribArray(0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// Delete the VBO
		for (int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}

		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);

		ALL_LOADED_MESHES.remove(ID);
	}

	protected int loadNormals(float[] normals, int index) {
		FloatBuffer norBuf = MemoryUtil.memAllocFloat(normals.length);
		norBuf.put(normals);
		norBuf.flip();

		int id;

		// Creating VBO
		id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		// Loading to VBO
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, norBuf, getStoreMode());
		GL20.glVertexAttribPointer(index, 3, GL11.GL_FLOAT, false, 0, 0);

		// disabling VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		MemoryUtil.memFree(norBuf);
		return id;
	}

	protected int loadVertices(float[] verts, int index) {
		FloatBuffer buf = MemoryUtil.memAllocFloat(verts.length);
		buf.put(verts);
		buf.flip();

		int id;

		id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		// Loading to VBO
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, getStoreMode());
		GL20.glVertexAttribPointer(index, 3, GL11.GL_FLOAT, false, 0, 0);

		// disabling VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		MemoryUtil.memFree(buf);
		return id;
	}

	protected int loadTexturePositions(float[] positions, int index) {
		FloatBuffer textBuf = MemoryUtil.memAllocFloat(positions.length);
		textBuf.put(positions);
		textBuf.flip();

		int id;

		id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		// Loading to VBO
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textBuf, getStoreMode());
		GL20.glVertexAttribPointer(index, 2, GL11.GL_FLOAT, false, 0, 0);

		// disabling VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		MemoryUtil.memFree(textBuf);
		return id;
	}

	protected int loadIndices(int[] inds) {
		IntBuffer indBuf = MemoryUtil.memAllocInt(inds.length);
		indBuf.put(inds);
		indBuf.flip();

		int id;
		id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indBuf, getStoreMode());

		// disabling VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		MemoryUtil.memFree(indBuf);
		return id;
	}

	public static final void massCleanUp() {
		Mesh[][] shapes = new Mesh[ALL_LOADED_MESHES.size()][];
		ALL_LOADED_MESHES.values().toArray(shapes);
		for (int i = 0; i < shapes.length; i++) {
			for (int i1 = 0; i1 < shapes[i].length; i1++) {
				shapes[i][i1].cleanup();
			}
		}

		ALL_LOADED_MESHES.clear();
	}

	public final void setAuthor(String author) {
		AUTHOR = author;
	}

	public static Mesh[] find(String name) {
		for (int key : ALL_LOADED_MESHES.keySet()) {
			if (name.equals(ALL_LOADED_MESHES.get(key)[0].NAME)) {
				return ALL_LOADED_MESHES.get(key);
			}
		}
		return null;
	}

	public final boolean renderWireFrame() {
		return renderWireFrame;
	}

	public final Mesh setRenderWireFrame(boolean renderWireFrame) {
		this.renderWireFrame = renderWireFrame;
		return this;
	}

	@Override
	public String toString() {
		return "Mesh [NAME=" + NAME + ", AUTHOR=" + AUTHOR + ", material=" + material + ", ID=" + ID
				+ ", renderWireFrame=" + renderWireFrame + ", vaoId=" + vaoId + ", vbos=" + vbos + ", TYPE=" + TYPE
				+ ", vertexQuantity=" + vertexQuantity + ", size=" + size + "]";
	}

}
