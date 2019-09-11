
package game_logic.map.badges;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import game_logic.map.Tile;
import game_logic.map.unit.Unit;
import graphicsSupport.texture.Texture;

public class Badge {

	// Location
	private Vector3f location;

	// Texture
	private Texture texture;

	// Color
	private Vector3i color;
	private int alpha = 255;

	public Badge(Tile tile, Texture texture, Vector3i color) {
		this.texture = texture;
		this.color = color;

		setTo(tile, Unit.badge_height);
	}

	public final Texture getTexture() {
		return texture;
	}

	public final Vector3i getColor() {
		return color;
	}

	public final Vector3f getLocation() {
		return location;
	}

	public final void setLocation(Vector3f location) {
		this.location = location;
	}

	public Vector3f set(float x, float y, float z) {
		return location.set(x, y, z);
	}

	public Vector3f set(Vector3fc v) {
		return location.set(v);
	}

	public void setTo(Tile tile, float height) {
		location = new Vector3f(tile.center);

		Vector3f height_vec = new Vector3f(0, height, 0);
		height_vec.rotate(tile.globalRotation);

		location.add(height_vec);
	}

	public final int getAlpha() {
		return alpha;
	}

	public final void setAlpha(int alpha) {
		this.alpha = alpha;
	}
}
