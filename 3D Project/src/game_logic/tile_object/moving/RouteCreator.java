package game_logic.tile_object.moving;

import org.joml.Vector2i;

import game_logic.map.Tile;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;

public abstract class RouteCreator {

	public RouteCreator() {
	}

	/**
	 * This method creates route by its algorithm and includes start and end
	 * tile;
	 * 
	 * @param obj
	 *            : movable object
	 * @param start
	 *            : starting point
	 * @param end
	 *            : ending point
	 * @param status
	 *            : status
	 * @return
	 */
	public abstract <T extends TileObject & Movable> Tile[] createRoute(T obj, Vector2i start, Vector2i end,
			RouteCreationStatus status);

	protected Vector2i[] decomposeDirection(Vector2i dir) {
		Vector2i[] result = new Vector2i[Math.max(Math.abs(dir.x), Math.abs(dir.y))];

		Vector2i direction = new Vector2i(dir);
		Vector2i path = new Vector2i();

		for (int i = 0; i < result.length; i++) {

			if (direction.x > 0) {
				path.x = 1;
			}
			if (direction.x == 0) {
				path.x = 0;
			}
			if (direction.x < 0) {
				path.x = -1;
			}

			if (direction.y > 0) {
				path.y = 1;
			}
			if (direction.y == 0) {
				path.y = 0;
			}
			if (direction.y < 0) {
				path.y = -1;
			}

			direction.add(-path.x, -path.y);
			result[i] = new Vector2i(path);
		}

		return result;
	}

	public static class RouteCreationStatus {

		// Statuses
		public static final int status_OK = 0;
		public static final int status_blocked = 2;

		// Status
		public int status;

		public RouteCreationStatus() {
		}

	}

}
