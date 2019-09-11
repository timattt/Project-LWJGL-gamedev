/**
 * 
 */
package graphicsSupport.effects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import engine.Engine;
import engine.WindowMatricesManager;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import graphicsSupport.camera.Camera;

/**
 * @author timat
 *
 */
@MonoDemeanor
public class DirectionalLightMatricesHandler {

	@MonoDemeanorInstance
	public static final DirectionalLightMatricesHandler instance = new DirectionalLightMatricesHandler();

	// Matrices storage
	private final Matrix4f[] orthoProjectionMatrices = new Matrix4f[Engine.getEngineOptions()
			.getDirectionalLightPerspectiveSplitsQuantity()];
	private final Matrix4f[] lightViewMatrices = new Matrix4f[Engine.getEngineOptions()
			.getDirectionalLightPerspectiveSplitsQuantity()];
	private final Vector4f[][] matricesPlanes = new Vector4f[Engine.getEngineOptions()
			.getDirectionalLightPerspectiveSplitsQuantity()][6];

	// Support fields
	private final Matrix4f cameraViewMatrix = new Matrix4f();
	private final Matrix4f projView = new Matrix4f();
	private final Vector3f averageVector = new Vector3f();
	private final Vector3f get = new Vector3f();
	private final Vector3f vec = new Vector3f();
	private final Vector4f tmpVec = new Vector4f();
	private final Vector4f plane = new Vector4f();

	/**
	 * 
	 */
	private DirectionalLightMatricesHandler() {
		for (int i = 0; i < orthoProjectionMatrices.length; i++) {
			orthoProjectionMatrices[i] = new Matrix4f();
			lightViewMatrices[i] = new Matrix4f();
			for (int a = 0; a < matricesPlanes[i].length; a++) {
				matricesPlanes[i][a] = new Vector4f();
			}
		}

	}

	public void updateMatrices(Camera cam) {
		DirectionalLight light = DirectionalLight.instance;

		float[] splits = Engine.getEngineOptions().getDirectional_light_splits();
		cameraViewMatrix.set(cam.getViewMatrix());

		float lightAngleX = (float) (Math.acos(light.getDirection().z));
		float lightAngleY = (float) (Math.asin(light.getDirection().x));

		float minX = Float.MAX_VALUE;
		float maxX = -Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MIN_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxZ = -Float.MIN_VALUE;

		for (int split_i = 0; split_i < splits.length; split_i++) {
			projView.identity();
			projView.setPerspective(Engine.getEngineOptions().getFov(), WindowMatricesManager.instance.getAspectRatio(),
					split_i > 0 ? splits[split_i - 1] : Engine.Z_NEAR, splits[split_i]);
			projView.mul(cameraViewMatrix);

			for (int i = 0; i < 6; i++) {
				projView.frustumPlane(i, plane);
				matricesPlanes[split_i][i].set(plane);
			}

			minZ = Float.MAX_VALUE;
			maxZ = -Float.MIN_VALUE;
			averageVector.zero();
			for (int i = 0; i < 8; i++) {

				projView.frustumCorner(i, get);
				averageVector.add(get);

				minZ = Math.min(minZ, get.z);
				maxZ = Math.max(maxZ, get.z);
			}
			averageVector.mul(0.125f);

			averageVector.add(vec.set(light.getDirection()).mul(maxZ - minZ));

			lightViewMatrices[split_i].identity().rotateX(lightAngleX).rotateY(lightAngleY).translate(-averageVector.x,
					-averageVector.y, -averageVector.z);
		}

		for (int split_i = 0; split_i < splits.length; split_i++) {
			minX = Float.MAX_VALUE;
			maxX = -Float.MIN_VALUE;
			minY = Float.MAX_VALUE;
			maxY = -Float.MIN_VALUE;
			minZ = Float.MAX_VALUE;
			maxZ = -Float.MIN_VALUE;

			projView.identity();
			projView.setPerspective(Engine.getEngineOptions().getFov(), WindowMatricesManager.instance.getAspectRatio(),
					split_i > 0 ? splits[split_i - 1] : Engine.Z_NEAR, splits[split_i]);
			projView.mul(cameraViewMatrix);

			for (int i = 0; i < 8; i++) {
				projView.frustumCorner(i, get);
				tmpVec.set(get, 1);
				tmpVec.mul(this.lightViewMatrices[split_i]);
				minX = Math.min(tmpVec.x, minX);
				maxX = Math.max(tmpVec.x, maxX);
				minY = Math.min(tmpVec.y, minY);
				maxY = Math.max(tmpVec.y, maxY);
				minZ = Math.min(tmpVec.z, minZ);
				maxZ = Math.max(tmpVec.z, maxZ);
			}
			float distz = maxZ - minZ;

			orthoProjectionMatrices[split_i].setOrtho(minX, maxX, minY, maxY, 0, distz);
		}
	}

	public final Matrix4f getLightViewMatrix(int i) {
		return lightViewMatrices[i];
	}

	public final Matrix4f getOrthoProjectionMatrix(int i) {
		return orthoProjectionMatrices[i];
	}

	public final Matrix4f[] getOrthoProjectionMatrices() {
		return orthoProjectionMatrices;
	}

	public final Matrix4f[] getLightViewMatrices() {
		return lightViewMatrices;
	}

	public final Vector4f[][] getMatricesPlanes() {
		return matricesPlanes;
	}

}
