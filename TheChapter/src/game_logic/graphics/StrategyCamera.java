package game_logic.graphics;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import controlSupport.MouseHandler;
import engine.Engine;
import engine.Window;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorUpdate;
import game_logic.GameOptions;
import game_logic.gui.events.Delegate;
import game_logic.gui.events.Events;
import game_logic.gui.events.EventsBundle;
import game_logic.gui.events.EventsCreator;
import game_logic.gui.events.EventsListener;
import game_logic.gui.events.EventsSystem;
import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.TileSizeHandler;
import graphicsSupport.camera.Camera;

@MonoDemeanor
public class StrategyCamera extends Camera implements EventsListener, EventsBundle {

	@MonoDemeanorInstance
	public static final StrategyCamera instance = new StrategyCamera();
	
	// Constants
	private final float wheel_step = TileSizeHandler.instance.getTileSize() / 10f;

	// Hot keys
	private int key_forward = GLFW.GLFW_KEY_W;
	private int key_backward = GLFW.GLFW_KEY_S;
	private int key_right = GLFW.GLFW_KEY_D;
	private int key_left = GLFW.GLFW_KEY_A;

	private int key_rotate_right = GLFW.GLFW_KEY_X;
	private int key_rotate_left = GLFW.GLFW_KEY_Z;
	private int key_reset_rotation = GLFW.GLFW_KEY_SPACE;

	// Speed
	private final float camera_speed = TileSizeHandler.instance.getTileSize() / 600f;
	private final float camera_rotation_speed = 1f;

	private boolean lock = false;

	// Rotation
	private final Vector3f rotation = new Vector3f(50, 90, 0);

	// Wheel
	private float currentWheelPosition = 0;
	private float positiveWheelPosition;
	private float lastY = 0;

	// Delegate
	private Delegate delegate = new Delegate() {

		@Override
		public void newEvent(int index) {
		}

		@Override
		public void premise(Object premise, int index) {
			if (index == Events.PREMISE_NEW_MAP) {
				map = (Map) premise;
			}
		}

	};

	// Map
	private Map map;

	private StrategyCamera() {
		setRotation(rotation);
		setPosition(10, 10, 10);
		EventsSystem.instance.addBundle(this);
	}

	@MonoDemeanorUpdate
	public void update() {
		if (lock) {
			return;
		}
		MouseHandler hand = MouseHandler.instance;
		long windowId = Window.instance.getWindowID();

		float camera_speed;
		camera_speed = this.camera_speed * Math.max(1, Math.abs(this.getPosition().y));

		float delta = hand.getWheelPosition() - currentWheelPosition;
		currentWheelPosition = hand.getWheelPosition();
		positiveWheelPosition = Math.max(0, this.positiveWheelPosition + delta);

		// Keys
		// Forward
		if (GLFW.glfwGetKey(windowId, key_forward) == GLFW.GLFW_PRESS) {
			movePosition(0, 0, -camera_speed);
		}

		// Backward
		if (GLFW.glfwGetKey(windowId, key_backward) == GLFW.GLFW_PRESS) {
			movePosition(0, 0, camera_speed);
		}

		// Right
		if (GLFW.glfwGetKey(windowId, key_right) == GLFW.GLFW_PRESS) {
			movePosition(camera_speed, 0, 0);
		}

		// Left
		if (GLFW.glfwGetKey(windowId, key_left) == GLFW.GLFW_PRESS) {
			movePosition(-camera_speed, 0, 0);
		}

		// Rotation
		// Right
		if (GLFW.glfwGetKey(windowId, key_rotate_right) == GLFW.GLFW_PRESS) {
			moveRotation(0, camera_rotation_speed, 0);
		}
		if (GLFW.glfwGetKey(windowId, key_rotate_left) == GLFW.GLFW_PRESS) {
			moveRotation(0, -camera_rotation_speed, 0);
		}
		if (GLFW.glfwGetKey(windowId, key_reset_rotation) == GLFW.GLFW_PRESS) {
			setRotation(rotation);
		}

		float y_map;
		try {
			y_map = (map == null ? 0 : map.projectY(getPosition()))
					+ ((GameOptions) Engine.getExternalOptions()).getCameraFromGround();
		} catch (Exception e) {
			y_map = lastY;
		}

		if (((GameOptions) Engine.getExternalOptions()).isCamera_bounded_to_terrain()) {
			setY(y_map + positiveWheelPosition * wheel_step);
		} else {
			setY(positiveWheelPosition * wheel_step);
		}

		if (getPosition().y < y_map) {
			setY(y_map);
			positiveWheelPosition = y_map / wheel_step;
		}

		lastY = y_map;
	}

	@Override
	public Delegate getDelegate() {
		return delegate;
	}

	@Override
	public EventsCreator[] getEventsCreators() {
		return null;
	}

	@Override
	public EventsListener[] getEventsListeners() {
		return new EventsListener[] { this };
	}

	public void concentrateOnTile(Tile tile) {
		setPosition(tile.center.x, tile.center.y + TileSizeHandler.instance.getTileSize() * 3f, tile.center.z);
		setRotation(90, 90, 0);
		lock = true;
	}

	public void unconcentrate() {
		if (lock) {
			setRotation(rotation);
			movePosition(0f, 0f, TileSizeHandler.instance.getTileSize() * 1.5f);
		}
		lock = false;
	}
}
