package game_logic.gui.controllers.selectors;

import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import controlSupport.MouseHandler;
import engine.WindowMatricesManager;
import game_logic.graphics.StrategyCamera;
import game_logic.map.Map;
import game_logic.map.Tile;
import graphicsSupport.camera.Camera;

public class TileSelector {

	private boolean left_mouse_button_pressed;
	private boolean right_mouse_button_pressed;

	public final void selectTile(Map map) {
		MouseHandler hand = MouseHandler.instance;

		// Pressed
		if (hand.leftMouseButtonPressed()) {
			left_mouse_button_pressed = true;
		}
		if (hand.rightMouseButtonPressed()) {
			right_mouse_button_pressed = true;
		}
		// Clicked
		if (!hand.leftMouseButtonPressed() && left_mouse_button_pressed) {
			left_mouse_button_pressed = false;
			Tile selected = select(map);
			if (selected != null) {
				leftMouseButtonClickedTile(selected);
			}

		}
		if (!hand.rightMouseButtonPressed() && right_mouse_button_pressed) {
			right_mouse_button_pressed = false;
			Tile selected = select(map);
			if (selected != null) {
				rightMouseButtonClickedTile(selected);
			}
		}
	}

	public final Tile select(Map map) {
		MouseHandler hand = MouseHandler.instance;

		// Finding ray
		Vector3f[] ray = findSelectionRay(hand.getMousePosition(), StrategyCamera.instance);
		LinkedList<Tile> inters = new LinkedList<Tile>();

		// Finding tiles witch intersects
		for (int x = 0; x < map.getWidth(); x++) {
			for (int z = 0; z < map.getHeight(); z++) {
				if (map.getTile(x, z).intersectsRay(ray[0], ray[1])) {
					inters.add(map.getTile(x, z));
				}
			}
		}

		// Finding nearest
		float min_length = Float.MAX_VALUE;
		float tile_length;
		Tile selected = null;
		for (Tile tile : inters) {
			tile_length = tile.getLength(StrategyCamera.instance.getPosition());
			if (tile_length < min_length) {
				min_length = tile_length;
				selected = tile;
			}
		}

		return selected;
	}

	/**
	 * result[0] - position; result[1] - direction;
	 * 
	 * @return
	 */
	private Vector3f[] findSelectionRay(Vector2f screen_pos, Camera cam) {

		Vector3f[] result = new Vector3f[2];

		// Finding start position
		float x = (float) (2 * screen_pos.x - 1);
		float y = (float) (1 - 2 * screen_pos.y);
		Vector4f full_screen_pos = new Vector4f(x, y, -1f, 1);

		// Projection matrix
		Matrix4f inv_proj = new Matrix4f(WindowMatricesManager.instance.getProjectionMatrix());
		inv_proj.invert();

		full_screen_pos.mul(inv_proj);
		full_screen_pos = new Vector4f(full_screen_pos.x, full_screen_pos.y, -1f, 0f);

		// View matrix
		Matrix4f inv_view = new Matrix4f(cam.getViewMatrix());
		inv_view.invert();

		full_screen_pos.mul(inv_view);
		full_screen_pos.normalize();

		// Putting result into array
		result[0] = new Vector3f(cam.getPosition());
		result[1] = new Vector3f(full_screen_pos.x, full_screen_pos.y, full_screen_pos.z);

		return result;
	}

	public void rightMouseButtonClickedTile(Tile tile) {
	}

	public void leftMouseButtonClickedTile(Tile tile) {
	}

}
