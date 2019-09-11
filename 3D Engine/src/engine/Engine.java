package engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import engine.monoDemeanor.MonoDemeanorHandler;
import engine.options.EngineOptions;
import engine.options.Options;
import frameBuffer.FrameBuffer;
import graphicsSupport.Renderer;
import graphicsSupport.Universe;
import graphicsSupport.camera.Camera;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.shaders.ShaderProgram;
import graphicsSupport.texture.Texture;
import soundSupport.SoundBuffer;
import soundSupport.SoundManager;
import utilities.Console;

public class Engine {

	// Constants
	public static final float Z_NEAR = 0.1f;

	// Resources path
	private static String RESOURCES_PATH;
	private static String LIBRARY_RESOURCES_PATH;

	private static String ENGINE_FILE_PATH;
	private static String PROJECT_FILE_PATH;

	// Options
	private static EngineOptions options;
	private static Options externalOptions;

	// Mono demeanor
	private static MonoDemeanorHandler monoDemeanorHandler;

	// Thread
	private static int current_fps;
	private static Timer timer;
	private static Thread gameThread;
	private static boolean running = true;

	private static Runnable mainLoop = new Runnable() {
		@Override
		public void run() {
			// Initialization
			try {
				timer.init();
				monoDemeanorHandler = new MonoDemeanorHandler(ENGINE_FILE_PATH, PROJECT_FILE_PATH);
				monoDemeanorHandler.preInitMonoDemeanors();
				monoDemeanorHandler.init();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// FPS variables
			float elapsedTime;
			float accumulator = 0f;
			float interval = 1f / options.getTargetFps();

			// FPS counter
			FPScounter counter = new FPScounter();

			long windowID = Window.instance.getWindowID();

			GL11.glClearColor(0f, 0f, 0f, 0f);

			try {

				while (!GLFW.glfwWindowShouldClose(windowID) && running) {

					counter.check();

					elapsedTime = timer.getElapsedTime();
					accumulator += elapsedTime;
					// Clear
					GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

					while (accumulator >= interval) {
						monoDemeanorHandler.update();
						accumulator -= interval;
					}

					Renderer.instance.render(Universe.instance, Camera.CURRENT_CAMERA);

					// Synchronization
					if (!options.v_sync()) {
						sync();
					}

					// Window update
					GLFW.glfwSwapBuffers(windowID);
					GLFW.glfwPollEvents();
					counter.tick();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			running = false;
			try {
				monoDemeanorHandler.cleanup();
				Mesh.massCleanUp();
				ShaderProgram.masscleanup();
				Texture.masscleanup();
				SoundBuffer.masscleanup();
				FrameBuffer.massCleanup();

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				options.createNewOptionsFile();
				if (externalOptions != null) {
					externalOptions.createNewOptionsFile();
				}
			} catch (IOException e) {
				Console.println("Unable to create new options file!");
			}
		}

		private void sync() {
			float loopSlot = 1f / options.getTargetFps();
			double endTime = timer.getLastLoopTime() + loopSlot;
			while (timer.getTime() < endTime) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException ie) {
				}
			}
		}
	};

	public static void initTEngine(Class<?> home, String options, Class<? extends Options> extOpts,
			String extOptsPath) {
		RESOURCES_PATH = loadPathToResources(home);
		LIBRARY_RESOURCES_PATH = loadPathToResources(Engine.class);

		try {
			ENGINE_FILE_PATH = Engine.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			PROJECT_FILE_PATH = home.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		}

		Console.println("Engine resources path: " + LIBRARY_RESOURCES_PATH);
		Console.println("Project resources path: " + RESOURCES_PATH);
		Console.println("Engine location: " + ENGINE_FILE_PATH);
		Console.println("Project location: " + PROJECT_FILE_PATH);

		try {
			Engine.options = (EngineOptions) Options.loadOptions(EngineOptions.class,
					Engine.getResourceAsFile(options));
		} catch (Exception e) {
			Console.println_err("Failed to load engine options!");
			try {
				Engine.options = (EngineOptions) Options.createNewOptions(EngineOptions.class,
						Engine.getResourceAsFile(options));
			} catch (InstantiationException | IllegalAccessException | IOException e1) {
				e1.printStackTrace();
			}
		}

		if (extOpts != null) {
			try {
				Engine.externalOptions = Options.loadOptions(extOpts, Engine.getResourceAsFile(extOptsPath));
			} catch (Exception e) {
				Console.println_err("Failed to load external options!");
				try {
					Engine.externalOptions = Options.createNewOptions(extOpts, Engine.getResourceAsFile(extOptsPath));
				} catch (InstantiationException | IllegalAccessException | IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		gameThread = new Thread(mainLoop, "Engine");
		timer = new Timer();
		gameThread.start();
	}

	public static File getResourceAsFile(String path) {
		return new File(RESOURCES_PATH + path);
	}

	public static File getLibraryResourceAsFile(String path) {
		return new File(LIBRARY_RESOURCES_PATH + path);
	}

	public static InputStream getLibraryResourceAsStream(String path) {
		try {
			return new FileInputStream(LIBRARY_RESOURCES_PATH + path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static InputStream getResourceAsStream(String path) {
		try {
			return new FileInputStream(RESOURCES_PATH + path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

	}

	private static final String loadPathToResources(Class<?> home) {
		String path = null;
		try {
			path = home.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		path = path.replace("/bin", "");
		if (!new File(path).isDirectory()) {
			path = new File(path).getParent() + "/Resources";
		} else {
			path = path + "Resources";
		}

		// Finding resources
		if (!new File(path).exists()) {
			System.err.println("Resources file missed! " + path);
			return null;
		}
		return path;
	}

	public static final float getRenderDistance() {
		return options.getRender_range() - Z_NEAR;
	}

	public static final boolean isRunning() {
		return running;
	}

	public static final int getCurrent_fps() {
		return current_fps;
	}

	public static final EngineOptions getEngineOptions() {
		return options;
	}

	public static final Options getExternalOptions() {
		return externalOptions;
	}

	public static final void stop() {
		running = false;
	}

	private static class FPScounter {

		// Ticks
		private int ticks = 0;

		// Time
		private long startTime = -1;

		// FPS
		public int last_fps;

		public FPScounter() {
		}

		public void tick() {
			ticks++;
		}

		public void check() {

			if (startTime == -1) {
				startTime = System.currentTimeMillis();
				return;
			}
			if (System.currentTimeMillis() - startTime > 1000) {
				last_fps = ticks;
				current_fps = last_fps;
				ticks = 0;
				startTime = System.currentTimeMillis();

				if (options.printSoundLog()) {
					SoundManager.instance.logSoundCongestion();
				}
				if (options.printFPS()) {
					Console.println("FPS: " + last_fps);
				}
			}
		}

	}
}
