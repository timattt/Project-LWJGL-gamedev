/**
 * 
 */
package graphicsSupport.camera;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import controlSupport.MouseHandler;
import engine.Window;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorUpdate;

/**
 * @author timat
 *
 */
@MonoDemeanor
public class PolarCamera extends Camera {
	
	@MonoDemeanorInstance
	public static final PolarCamera instance = new PolarCamera();
	
	// Controlling camera
	private Vector2f startPos = new Vector2f(0f, 0f);
	private Vector2f delta = new Vector2f();
	private Vector2f oldRot = new Vector2f();

	//private float lastWheelPos = 0f;
	private float radius = 4f;

	// Rotation using polar coordinates
	private Vector2f polar = new Vector2f(3.7f, 0.5f);

	private boolean firstUpdate = true;
	
	protected PolarCamera() {
		position.y = 1.75f;
	}

	@MonoDemeanorUpdate
	public void update() {
		float speed = 0.1f;
		long windowId = Window.instance.getWindowID();

		if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
			movePosition(speed, 0, 0);
		}
		if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS) {
			movePosition(0, speed, 0);
		}
		if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
			movePosition(0, 0, -speed);
		}

		if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
			movePosition(0, 0, speed);
		}
		if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
			movePosition(-speed, 0, 0);
		}
		if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_F) == GLFW.GLFW_PRESS) {
			movePosition(0, -speed, 0);
		}

		/*
		if (lastWheelPos != MouseHandler.instance.getWheelPosition()) {
			//radius += (MouseHandler.instance.getWheelPosition() - lastWheelPos) / 5f;
			calculateSpherePosition(0f, 0f);
			lastWheelPos = MouseHandler.instance.getWheelPosition();
		}
		*/
		if (MouseHandler.instance.leftMouseButtonPressed() || firstUpdate) {
			firstUpdate = false;
			if (startPos.x == -10f) {
				startPos.set(MouseHandler.instance.getMousePosition());
				oldRot.zero();
			} else {
				delta.set(MouseHandler.instance.getMousePosition());
				delta.negate();
				delta.add(startPos);
				delta.negate();

				float deltaX = (float) ((delta.x - oldRot.x) * Math.PI);
				float deltaY = (float) ((delta.y - oldRot.y) * Math.PI);

				oldRot.set(delta);

				calculateSpherePosition(deltaX, deltaY);
			}
		} else {
			startPos.x = -10f;
		}
	}

	private void calculateSpherePosition(float deltaX, float deltaY) {

		polar.x += deltaX;
		polar.y += deltaY;

		rotation.x = (float) Math.toDegrees(polar.y);
		rotation.y = (float) Math.toDegrees(polar.x);

		viewMatrix.identity();
		viewMatrix.translate(0, 0, -radius);
		viewMatrix.rotateX(polar.y);
		viewMatrix.rotateY(polar.x);
		viewMatrix.translate(-position.x, -position.y, -position.z);

	}

	public final void setRadius(float radius) {
		this.radius = radius;
	}

}
