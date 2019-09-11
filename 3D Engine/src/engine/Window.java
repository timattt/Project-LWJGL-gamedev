/**
 * 
 */
package engine;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorPriority;
import engine.options.EngineOptions;

/**
 * @author timat
 *
 */
@MonoDemeanor()
public class Window {

	@MonoDemeanorInstance
	public static final Window instance = new Window();

	// Window id and name
	private long windowID;
	private static final String NAME = "3d engine";

	// Size
	private int WIDTH;
	private int HEIGHT;

	private Window() {
	}

	@MonoDemeanorInit(priority = MonoDemeanorPriority.HIGH)
	public void initWindow() {
		EngineOptions options = Engine.getEngineOptions();
		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, Engine.getEngineOptions().getContextVersionMajor());
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, Engine.getEngineOptions().getContextVersionMinor());
		glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
		if (options.isCompatible_profile()) {
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
		} else {
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		}
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		
		WIDTH = d.width;
		HEIGHT = d.height;
		if (options.full_screen()) {
			windowID = glfwCreateWindow(WIDTH, HEIGHT, NAME, glfwGetPrimaryMonitor(), NULL);
		} else {
			windowID = glfwCreateWindow(WIDTH, HEIGHT, NAME, NULL, NULL);
		}

		if (windowID == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(windowID, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);

		glfwMakeContextCurrent(windowID);

		if (options.v_sync()) {
			glfwSwapInterval(1);
		}

		// Make the window visible
		glfwShowWindow(windowID);

		GL.createCapabilities();

		GLFW.glfwSetWindowSizeCallback(windowID, new GLFWWindowSizeCallbackI() {
			@Override
			public void invoke(long id, int w, int h) {
				GL11.glViewport(0, 0, w, h);
				WIDTH = w;
				HEIGHT = h - 1;
			}
		});

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Performance
		GL11.glClearDepth(1.0f);

		// Depth
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glDepthMask(true);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		if (options.cull_face()) {
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
		}
		if (options.antialiasing()) {
			glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
		}
	}

	public final long getWindowID() {
		return windowID;
	}

	public final int getWIDTH() {
		return WIDTH;
	}

	public final int getHEIGHT() {
		return HEIGHT;
	}

}
