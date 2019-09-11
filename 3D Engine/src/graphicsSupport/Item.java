package graphicsSupport;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import graphicsSupport.mesh.Mesh;
import utilities.Console;

public class Item {

	// Meshes
	protected Mesh[] meshes;

	// Matrix
	private final Vector3f position;
	private float scale;
	private final Vector3f rotation;
	private final Quaternionf rotationQuaternion;

	private Matrix4f modelMatrix;

	// Rendering parameters
	private volatile float colorScalar = 1f;
	private volatile boolean billboard;
	private volatile float alpha = 1f;
	private volatile float radius;

	public Item(Mesh[] mesh) {
		this.meshes = mesh;
		position = new Vector3f();
		modelMatrix = new Matrix4f();
		scale = 1f;
		rotation = new Vector3f();
		rotationQuaternion = new Quaternionf();
		updateModelMatrix();
	}

	public void addMeshes(Mesh[] meshes) {
		for (Mesh mesh : meshes) {
			addMesh(mesh);
		}
	}

	public void removeMeshes(Mesh[] meshes) {
		for (Mesh mesh : meshes) {
			removeMesh(mesh);
		}
	}

	public void addMesh(Mesh mesh) {
		Mesh[] res = new Mesh[meshes.length + 1];
		int i = 0;
		for (Mesh m : meshes) {
			res[i] = m;
			i++;
		}
		res[meshes.length] = mesh;
		meshes = res;
	}

	public void removeMesh(Mesh mesh) {
		Mesh[] res = new Mesh[meshes.length - 1];
		int i = 0;
		for (Mesh m : meshes) {
			if (m == mesh) {
				continue;
			}
			res[i] = m;
			i++;
		}
	}

	public final float getScale() {
		return scale;
	}

	public Item setScale(float scale) {
		this.scale = scale;
		updateModelMatrix();
		return this;
	}

	public Mesh[] getMeshes() {
		return meshes;
	}

	public final Mesh getBaseMesh() {
		return meshes[0];
	}

	public final Vector3f getPosition() {
		return position;
	}

	public final Vector3f getRotation() {
		return rotation;
	}

	public final boolean hasMeshes() {
		return meshes != null && meshes.length > 0;
	}

	public Item setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
		updateModelMatrix();
		return this;
	}

	public Item rotate(Quaternionf quat) {
		this.rotationQuaternion.set(quat);
		updateModelMatrix();
		return this;
	}

	public Item setRotation(float x, float y, float z) {
		this.rotation.x = x;
		this.rotation.y = y;
		this.rotation.z = z;
		updateModelMatrix();
		return this;
	}

	public Item setPosition(Vector3f vec) {
		position.set(vec.x, vec.y, vec.z);
		updateModelMatrix();
		return this;
	}

	public void movePosition(float x, float y, float z) {
		setPosition(position.x + x, position.y + y, position.z + z);
	}

	public void moveRotation(float x, float y, float z) {
		this.rotation.x = this.rotation.x + x;
		this.rotation.y = this.rotation.y + y;
		this.rotation.z = this.rotation.z + z;
		updateModelMatrix();
	}

	public void movePosition(Vector3f vec) {
		position.add(vec);
		updateModelMatrix();
	}

	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	public Vector3f getDefaultNormal() {
		Vector3f result = new Vector3f(0, 1, 0);
		return result;
	}

	public final Quaternionf getRotationQuaternion() {
		return rotationQuaternion;
	}

	public void updateModelMatrix() {
		if (Float.isNaN(position.x) || Float.isNaN(position.y) || Float.isNaN(position.z)) {
			Console.println_err("Object position is NAN!");
		}
		if (Float.isNaN(rotation.x) || Float.isNaN(rotation.y) || Float.isNaN(rotation.z)) {
			Console.println_err("Object rotation is NAN!");
		}

		// Matrix
		modelMatrix.identity().translate(position).rotate(rotationQuaternion)
				.rotateX((float) Math.toRadians(rotation.x)).rotateY((float) Math.toRadians(rotation.y))
				.rotateZ((float) Math.toRadians(rotation.z)).scale(scale);
		
		recalcRadius();
	}

	public final boolean isBillboard() {
		return billboard;
	}

	public final void setBillboard(boolean billboard) {
		this.billboard = billboard;
	}

	public final float getColorScalar() {
		return colorScalar;
	}

	public final void setColorScalar(float colorScalar) {
		this.colorScalar = colorScalar;
	}

	public final float getAlpha() {
		return alpha;
	}

	public final void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	private void recalcRadius() {
		for (int i = 0; i < meshes.length; i++) {
			radius = Math.max(radius, meshes[i].radius);
		}
	}

	public final boolean checkInFrustum(Vector4f[] planes) {
		for (int i = 0; i < planes.length; i++) {
			if ((planes[i].x * position.x + planes[i].y * position.y + planes[i].z * position.z + planes[i].w) <= -radius) {
				return false;
			}
		}
		return true;
	}
}
