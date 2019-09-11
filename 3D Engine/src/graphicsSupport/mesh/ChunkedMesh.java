package graphicsSupport.mesh;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import graphicsSupport.Item;
import graphicsSupport.camera.Camera;
import graphicsSupport.effects.DirectionalLightMatricesHandler;
import graphicsSupport.effects.Material;
import graphicsSupport.shaders.ShaderProgram;

public class ChunkedMesh extends Mesh {

	// Storage parameters
	private static final int VECTOR4F_SIZE_BYTES = 4 * 4;
	private static final int MATRIX_SIZE_BYTES = 4 * VECTOR4F_SIZE_BYTES;
	private static final int MATRIX_SIZE_FLOATS = 4 * 4;

	// Chunk size
	protected final int chunkSize;

	// Buffers
	private FloatBuffer modelMatrixBuffer;

	// VBO
	private int modelMatrixVBO;

	public ChunkedMesh(float[] verts, float[] normals, int[] inds, String name, int chunkSize, Material mat, int id) {
		super(verts, normals, inds, name, mat, id);
		this.chunkSize = chunkSize;
		TYPE = 2;

		GL30.glBindVertexArray(vaoId);

		// Model matrix
		modelMatrixVBO = glGenBuffers();
		vbos.add(modelMatrixVBO);
		modelMatrixBuffer = MemoryUtil.memAllocFloat(chunkSize * MATRIX_SIZE_FLOATS);
		glBindBuffer(GL_ARRAY_BUFFER, modelMatrixVBO);
		int start = 4;
		for (int i = 0; i < 4; i++) {
			glVertexAttribPointer(start, 4, GL_FLOAT, false, MATRIX_SIZE_BYTES, i * VECTOR4F_SIZE_BYTES);
			glVertexAttribDivisor(start, 1);
			start++;
		}

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	public ChunkedMesh(float[] verts, float[] normals, float[] textPos, int[] inds, String name, int chunkSize,
			Material mat, int id) {
		super(verts, normals, textPos, inds, name, mat, id);
		this.chunkSize = chunkSize;
		TYPE = 2;

		GL30.glBindVertexArray(vaoId);

		// Model matrix
		modelMatrixVBO = glGenBuffers();
		vbos.add(modelMatrixVBO);
		modelMatrixBuffer = MemoryUtil.memAllocFloat(chunkSize * MATRIX_SIZE_FLOATS);
		glBindBuffer(GL_ARRAY_BUFFER, modelMatrixVBO);
		int start = 4;
		for (int i = 0; i < 4; i++) {
			glVertexAttribPointer(start, 4, GL_FLOAT, false, MATRIX_SIZE_BYTES, i * VECTOR4F_SIZE_BYTES);
			glVertexAttribDivisor(start, 1);
			start++;
		}

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	@Override
	public void renderObjectsList(LinkedList<Item> objs, Camera cam) {
		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		// Enabling texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, (material.hasDiffuseTexture() ? material.getTexture().getGLId() : 0));
		GL20.glEnableVertexAttribArray(3);

		// Model matrix VBO
		int start = 4;
		int numElements = 4;
		for (int i = 0; i < numElements; i++) {
			glEnableVertexAttribArray(start + i);
		}

		// Rendering
		ShaderProgram.chunkedShader.setUniform("material", material);
		for (int i = 0; i < objs.size(); i += chunkSize) {
			int end = Math.min(objs.size(), i + chunkSize);
			List<Item> subList = objs.subList(i, end);

			renderShapesInstanced(subList, cam);
		}

		// Disable matrix VBO
		start = 4;
		numElements = 4;
		for (int i = 0; i < numElements; i++) {
			glDisableVertexAttribArray(start + i);
		}

		// Disabling textures
		GL20.glDisableVertexAttribArray(3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Disable normals VBO
		GL20.glDisableVertexAttribArray(1);

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	protected void renderShapesInstanced(List<Item> subList, Camera cam) {

		// Preparing buffers
		modelMatrixBuffer.clear();

		int size = 0;
		for (int i = 0; i < subList.size(); i++) {
			Item obj = subList.get(i);
			Matrix4f mat = obj.getModelMatrix();

			if (obj.isBillboard()) {
				cam.getViewMatrix().transpose3x3(mat);
			}

			mat.m33(obj.getColorScalar());

			mat.get(MATRIX_SIZE_FLOATS * i, modelMatrixBuffer);

			//mat.m33(1f);

			size++;
		}

		glBindBuffer(GL_ARRAY_BUFFER, modelMatrixVBO);
		glBufferData(GL_ARRAY_BUFFER, modelMatrixBuffer, GL_DYNAMIC_DRAW);

		if (material.hasDiffuseTexture() || hasUV) {
			glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, this.vertexQuantity, size);
		} else {
			glDrawElementsInstanced(GL11.GL_TRIANGLES, this.vertexQuantity, GL11.GL_UNSIGNED_INT, 0, size);
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	protected void renderDepthInstanced(List<Item> subList, Camera cam, int cascade) {

		// Preparing buffers
		modelMatrixBuffer.clear();

		int size = 0;
		boolean empty = true;
		for (int i = 0; i < subList.size(); i++) {
			Item obj = subList.get(i);

			if (!obj.checkInFrustum(DirectionalLightMatricesHandler.instance.getMatricesPlanes()[cascade])) {
				continue;
			}
			empty = false;

			Matrix4f mat = obj.getModelMatrix();

			if (obj.isBillboard()) {
				cam.getViewMatrix().transpose3x3(mat);
			}

			mat.get(MATRIX_SIZE_FLOATS * size, modelMatrixBuffer);
			size++;
		}

		if (empty) {
			return;
		}

		glBindBuffer(GL_ARRAY_BUFFER, modelMatrixVBO);
		glBufferData(GL_ARRAY_BUFFER, modelMatrixBuffer, GL_DYNAMIC_DRAW);

		if (material.hasDiffuseTexture() || hasUV) {
			glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, this.vertexQuantity, size);
		} else {
			glDrawElementsInstanced(GL11.GL_TRIANGLES, this.vertexQuantity, GL11.GL_UNSIGNED_INT, 0, size);
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void renderDepth(LinkedList<Item> objs, Camera cam, int cascade) {
		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Model matrix VBO
		int start = 4;
		int numElements = 4;
		for (int i = 0; i < numElements; i++) {
			glEnableVertexAttribArray(start + i);
		}

		for (int i = 0; i < objs.size(); i += chunkSize) {
			int end = Math.min(objs.size(), i + chunkSize);
			List<Item> subList = objs.subList(i, end);

			renderDepthInstanced(subList, cam, cascade);
		}

		// Disable matrix VBO
		start = 4;
		numElements = 4;
		for (int i = 0; i < numElements; i++) {
			glDisableVertexAttribArray(start + i);
		}

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (modelMatrixBuffer != null) {
			MemoryUtil.memFree(modelMatrixBuffer);
		}
	}

	@Override
	public String toString() {
		return "ChunkedMesh [chunkSize=" + chunkSize + ", modelMatrixBuffer=" + modelMatrixBuffer + ", modelMatrixVBO="
				+ modelMatrixVBO + ", NAME=" + NAME + ", AUTHOR=" + AUTHOR + ", material=" + material + ", ID=" + ID
				+ ", vaoId=" + vaoId + ", vbos=" + vbos + ", TYPE=" + TYPE + ", vertexQuantity=" + vertexQuantity
				+ ", size=" + size + "]";
	}

}
