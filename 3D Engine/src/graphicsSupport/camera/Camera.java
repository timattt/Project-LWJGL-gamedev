package graphicsSupport.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorUpdate;

@MonoDemeanor
public class Camera {
	
	@MonoDemeanorInstance
	public static Camera CURRENT_CAMERA = PolarCamera.instance;
	
	// Location
	protected final Vector3f position;
	protected final Vector3f rotation;

	// View matrix
	protected Matrix4f viewMatrix;

	protected Camera() {
		position = new Vector3f(0, 0, 0);
		rotation = new Vector3f(0, 0, 0);
		viewMatrix = new Matrix4f();
		updateMatrix();
	}
	
	@MonoDemeanorUpdate
	public void updateCurrentCamera() {
		CURRENT_CAMERA.update();
	}
	
	protected void update() {
		
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
		updateMatrix();
	}

	public void movePosition(float offsetX, float offsetY, float offsetZ) {
		if (offsetZ != 0) {
			position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
			position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
		}
		if (offsetX != 0) {
			position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
			position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
		}
		position.y += offsetY;
		updateMatrix();
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(float x, float y, float z) {
		rotation.x = x;
		rotation.y = y;
		rotation.z = z;
		updateMatrix();
	}

	public void setRotation(Vector3f vec) {
		rotation.x = vec.x;
		rotation.y = vec.y;
		rotation.z = vec.z;
		updateMatrix();
	}

	public void moveRotation(float offsetX, float offsetY, float offsetZ) {
		rotation.x += offsetX;
		rotation.y += offsetY;
		rotation.z += offsetZ;
		updateMatrix();
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	private void updateMatrix() {
		// First do the rotation so camera rotates over its position
		viewMatrix.identity();
		viewMatrix.rotateX((float) Math.toRadians(rotation.x)).rotateY((float) Math.toRadians(rotation.y));
		// Then do the translation
		viewMatrix.translate(-position.x, -position.y, -position.z);
	}

	public void setX(float x) {
		position.x = x;
		updateMatrix();
	}

	public void setY(float y) {
		position.y = y;
		updateMatrix();
	}

	public void setZ(float z) {
		position.z = z;
		updateMatrix();
	}
}