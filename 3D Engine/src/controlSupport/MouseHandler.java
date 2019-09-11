package controlSupport;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallbackI;

import engine.Window;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import utilities.Console;

@MonoDemeanor
public class MouseHandler {

	@MonoDemeanorInstance
	public static final MouseHandler instance = new MouseHandler();

	// Mouse position
	private volatile Vector2f currentPos;
	private volatile Vector2f lastPos;

	// Buttons
	private volatile boolean right_button_pressed;
	private volatile boolean left_button_pressed;

	// Mouse wheel
	private volatile double wheel_pos = 1;

	private MouseHandler() {
		currentPos = new Vector2f();
		lastPos = new Vector2f();
	}

	@MonoDemeanorInit
	public void init() {
		long windowId = Window.instance.getWindowID();
		GLFW.glfwSetCursorPosCallback(windowId, new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				currentPos.x = (float) (xpos / Window.instance.getWIDTH());
				currentPos.y = (float) (ypos / Window.instance.getHEIGHT());
			}
		});
		GLFW.glfwSetMouseButtonCallback(windowId, new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				// Press
				if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
					left_button_pressed = true;
				}
				if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_PRESS) {
					right_button_pressed = true;
				}

				// Release
				if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE) {
					left_button_pressed = false;
				}
				if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_RELEASE) {
					right_button_pressed = false;
				}
			}
		});
		GLFW.glfwSetScrollCallback(windowId, new GLFWScrollCallbackI() {
			@Override
			public void invoke(long windowID, double x, double y) {
				wheel_pos = wheel_pos + y;
			}
		});

	}

	public Vector2f updateMousePosition() {
		double xPos = currentPos.x;
		double yPos = currentPos.y;

		if (xPos < 0 | xPos > Window.instance.getWIDTH()) {
			return new Vector2f();
		}
		if (yPos < 0 | yPos > Window.instance.getHEIGHT()) {
			return new Vector2f();
		}
		Console.printVector(currentPos);
		Console.printVector(lastPos);
		float x1 = (float) (xPos - lastPos.x);
		float y1 = (float) (yPos - lastPos.y);

		lastPos.x = currentPos.x;
		lastPos.y = currentPos.y;

		return new Vector2f().add(x1 / Window.instance.getWIDTH(), y1 / Window.instance.getHEIGHT());
	}

	public Vector2f getMousePosition() {
		return currentPos;
	}

	public boolean rightMouseButtonPressed() {
		return right_button_pressed;
	}

	public boolean leftMouseButtonPressed() {
		return left_button_pressed;
	}

	public float getWheelPosition() {
		return (float) wheel_pos;
	}

}
