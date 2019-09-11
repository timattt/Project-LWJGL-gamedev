/**
 * 
 */
package engine;

import org.joml.Matrix4f;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.options.EngineOptions;

/**
 * @author timat
 *
 */
@MonoDemeanor
public class WindowMatricesManager {

	@MonoDemeanorInstance
	public static final WindowMatricesManager instance = new WindowMatricesManager();
	
	// Matrices
	private final Matrix4f projectionMatrix = new Matrix4f();
	private final Matrix4f orthoMatrix = new Matrix4f();

	// Aspect ratio
	private float aspectRatio;
	
	/**
	 * 
	 */
	private WindowMatricesManager() {
	}
	
	@MonoDemeanorInit
	public void prepareMatrices() {
		Window w = Window.instance;
		EngineOptions options = Engine.getEngineOptions();
		projectionMatrix.identity();
		orthoMatrix.identity();
		aspectRatio = (float) ((float) w.getWIDTH() / (float) w.getHEIGHT());
		orthoMatrix.setOrtho(-50.0f, 50.0f, -50.0f, 50.0f, -10.0f, 50.0f);
		projectionMatrix.perspective(options.getFov(), aspectRatio, Engine.Z_NEAR, options.getRender_range());
	}

	public final Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public final Matrix4f getOrthoMatrix() {
		return orthoMatrix;
	}

	public final float getAspectRatio() {
		return aspectRatio;
	}

}
