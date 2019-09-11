package graphicsSupport.mesh.animation;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import graphicsSupport.Item;
import graphicsSupport.camera.Camera;
import graphicsSupport.effects.Material;
import graphicsSupport.mesh.ChunkedMesh;
import graphicsSupport.shaders.ShaderProgram;

public class ChunkedAnimation extends ChunkedMesh {

	// Frames
	private int framesQuantity;
	private float[][] vertices;
	private float[][] normals;

	// Buffers
	private int vertsBufferVBO;
	private int normalsBufferVBO;

	// Time
	private long startTime;
	private long[] framesTime;
	private long totalTime;

	public ChunkedAnimation(float[][] verts, float[][] normals, int[] inds, String name, Material mat, int id,
			long[] frameTimes, int chunk) {
		super(verts[0], normals[0], inds, name, chunk, mat, id);

		framesQuantity = frameTimes.length;
		this.framesTime = frameTimes;
		this.vertices = verts;
		this.normals = normals;

		calcTotalTime();
		startAnimation();

		TYPE = CHUNKED_MESH;
	}

	public ChunkedAnimation(float[][] verts, float[][] normals, float[] textPos, int[] inds, String name, Material mat,
			int id, long[] frameTimes, int chunk) {
		this(verts, normals, inds, name, mat, id, frameTimes, chunk);
	}

	@Override
	protected int getStoreMode() {
		return GL15.GL_STREAM_DRAW;
	}

	private void calcTotalTime() {
		totalTime = 0;
		for (int i = 0; i < framesTime.length; i++) {
			totalTime = totalTime + framesTime[i];
		}
	}

	@Override
	protected int loadNormals(float[] normals, int index) {
		return normalsBufferVBO = super.loadNormals(normals, index);
	}

	@Override
	protected int loadVertices(float[] verts, int index) {
		return vertsBufferVBO = super.loadVertices(verts, index);
	}

	public void startAnimation() {
		startTime = System.currentTimeMillis();
	}

	public void sync(long sync) {
		startTime = sync;
	}

	@Override
	public void renderObjectsList(LinkedList<Item> objs, Camera cam) {

		// Enabling VAO
		GL30.glBindVertexArray(vaoId);
		loadAnimData();

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, (material.hasDiffuseTexture() ? material.getTexture().getGLId() : 0));
		GL20.glEnableVertexAttribArray(3);

		// Model matrix VBO
		int start = 4;
		int numElements = 4;
		for (int i = 0; i < numElements; i++) {
			glEnableVertexAttribArray(start + i);
		}

		ShaderProgram.chunkedShader.setUniform("material", material);

		int length = objs.size();
		for (int i = 0; i < length; i += chunkSize) {
			int end = Math.min(length, i + chunkSize);
			List<Item> subList = objs.subList(i, end);

			renderShapesInstanced(subList, cam);
		}

		// Disable matrix VBO
		start = 4;
		numElements = 4;
		for (int i = 0; i < numElements; i++) {
			glDisableVertexAttribArray(start + i);
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

	private void loadAnimData() {
		float scalar = findFrameScalar();
		float[] a0 = vertices[getCurrentFrame()];
		float[] a1 = vertices[getNextFrame()];
		float[] res = new float[a0.length];

		float val;

		for (int i = 0; i < a0.length; i++) {
			val = a0[i] + (a1[i] - a0[i]) * scalar;
			res[i] = val;
		}

		glBindBuffer(GL_ARRAY_BUFFER, this.vertsBufferVBO);
		GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, res);

		a0 = normals[getCurrentFrame()];
		a1 = normals[getNextFrame()];
		res = new float[a0.length];

		for (int i = 0; i < a0.length; i++) {
			val = a0[i] + (a1[i] - a0[i]) * scalar;
			res[i] = val;
		}

		glBindBuffer(GL_ARRAY_BUFFER, this.normalsBufferVBO);
		GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, res);

		GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private int getNextFrame() {
		int next = getCurrentFrame() + 1;
		if (next == framesQuantity) {
			next = 0;
		}
		return next;
	}

	private float findFrameScalar() {
		long curr = System.currentTimeMillis();
		long bottom = getCurrentFrameStartPoint();
		float x = curr - bottom;
		float y = (bottom + framesTime[getCurrentFrame()]) - curr;
		return x / (x + y);
	}

	private long getCurrentFrameStartPoint() {
		long curr = System.currentTimeMillis();
		long time = curr - ((curr - startTime) % totalTime);
		int max = getCurrentFrame();
		for (int i = 0; i < max; i++) {
			time = time + framesTime[i];
		}

		return time;
	}

	private int getCurrentFrame() {
		long curr = System.currentTimeMillis();
		long time = curr - ((curr - startTime) % totalTime);
		for (int i = 0; i < framesQuantity; i++) {
			if (time + framesTime[i] > curr) {
				return i;
			} else {
				time = time + framesTime[i];
			}
		}

		return -1;
	}

	@Override
	public String toString() {
		return "ChunkedAnimation [framesQuantity=" + framesQuantity + ", vertices=" + Arrays.toString(vertices)
				+ ", normals=" + Arrays.toString(normals) + ", vertsBufferVBO=" + vertsBufferVBO + ", normalsBufferVBO="
				+ normalsBufferVBO + ", startTime=" + startTime + ", framesTime=" + Arrays.toString(framesTime)
				+ ", totalTime=" + totalTime + ", chunkSize=" + chunkSize + ", NAME=" + NAME + ", AUTHOR=" + AUTHOR
				+ ", material=" + material + ", ID=" + ID + ", vaoId=" + vaoId + ", vbos=" + vbos + ", TYPE=" + TYPE
				+ ", vertexQuantity=" + vertexQuantity + ", size=" + size + "]";
	}

}
