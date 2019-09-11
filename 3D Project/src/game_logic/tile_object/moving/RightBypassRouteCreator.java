package game_logic.tile_object.moving;

import java.util.LinkedList;

import org.joml.Vector2i;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;

public class RightBypassRouteCreator extends RouteCreator {

	public RightBypassRouteCreator() {
	}

	@Override
	public <T extends TileObject & Movable> Tile[] createRoute(T obj, Vector2i start, Vector2i end,
			RouteCreationStatus status) {
		MapDirection dir = new MapDirection(start, end);
		dir.normalize();
		LinkedList<Tile> path = new LinkedList<Tile>();
		Map map = obj.getHomeMap();
		Vector2i pos = new Vector2i(start);
		LinearRouteCreator linear = new LinearRouteCreator();
		Tile[] linear_route = null;
		boolean end_tile_blocked = !map.getTile(end).isWalkable(obj);
		Tile tile_to_add;

		A : while (!(pos.x == end.x && pos.y == end.y)) {
			if (end_tile_blocked && Math.abs(pos.x - end.x) <= 1 && Math.abs(pos.y - end.y) <= 1) {
				status.status = RouteCreationStatus.status_blocked;
				break;
			}
			dir = new MapDirection(pos, end);
			dir.normalize();

			// Rotating direction until it finds walkable tile
			int i = 0;

			try {
				while (!map.getTile(dir.getPosition(pos)).isWalkable(obj)) {
					if (i == 8) {
						status.status = RouteCreationStatus.status_blocked;
						break A;
					}
					dir.rotateRightNormalized();
					i++;
				}
			} catch (Exception e) {
				break;
			}

			// Moving to founded point
			dir.move(pos);
			tile_to_add = map.getTile(pos);
			if (path.contains(tile_to_add)) {
				status.status = 2;
				break;
			}
			path.add(tile_to_add);

			// And then checking if unit can go linear
			linear_route = linear.createRoute(obj, pos, end, status);
			if (status.status == 0) {
				break;
			}
			
			linear_route = null;
		}

		Tile[] result = new Tile[path.size() + 1 + (linear_route != null ? linear_route.length - 1 : 0)];

		result[0] = map.getTile(start);
		for (int i = 0; i < result.length - 1; i++) {
			result[i + 1] = i < path.size() ? path.get(i) : linear_route[1 + i - path.size()];
		}

		if (result[result.length - 1] == map.getTile(end)) {
			status.status = RouteCreationStatus.status_OK;
		}

		return result;
	}

	protected class MapDirection {

		protected int x;
		protected int y;

		public MapDirection(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public MapDirection() {
		}

		public MapDirection(Vector2i start, Vector2i end) {
			x = end.x - start.x;
			y = end.y - start.y;
		}

		public final int getX() {
			return x;
		}

		public final void setX(int x) {
			this.x = x;
		}

		public final int getY() {
			return y;
		}

		public final void setY(int y) {
			this.y = y;
		}

		public final void normalize() {
			if (x != 0) {
				x = x / Math.abs(x);
			}
			if (y != 0) {
				y = y / Math.abs(y);
			}
		}

		public final void rotateRightNormalized() {
			normalize();
			if (x == 1 && y == 1) {
				x = 0;
				return;
			}
			if (x == 0 && y == 1) {
				x = -1;
				y = 1;
				return;
			}
			if (x == -1 && y == 1) {
				x = -1;
				y = 0;
				return;
			}
			if (x == -1 && y == 0) {
				x = -1;
				y = -1;
				return;
			}
			if (x == -1 && y == -1) {
				x = 0;
				y = -1;
				return;
			}
			if (x == 0 && y == -1) {
				x = 1;
				y = -1;
				return;
			}
			if (x == 1 && y == -1) {
				x = 1;
				y = 0;
				return;
			}
			if (x == 1 && y == 0) {
				x = 1;
				y = 1;
				return;
			}
		}

		public final void rotateLeftNormalized() {
			normalize();
			if (x == 1 && y == 1) {
				x = 1;
				y = 0;
				return;
			}
			if (x == 0 && y == 1) {
				x = 1;
				y = 1;
				return;
			}
			if (x == -1 && y == 1) {
				x = 0;
				y = 1;
				return;
			}
			if (x == -1 && y == 0) {
				x = -1;
				y = 1;
				return;
			}
			if (x == -1 && y == -1) {
				x = -1;
				y = 0;
				return;
			}
			if (x == 0 && y == -1) {
				x = -1;
				y = -1;
				return;
			}
			if (x == 1 && y == -1) {
				x = 0;
				y = -1;
				return;
			}
			if (x == 1 && y == 0) {
				x = 1;
				y = -1;
				return;
			}
		}

		public final Vector2i getPosition(Vector2i pos) {
			return new Vector2i(pos.x + x, pos.y + y);
		}

		public final void move(Vector2i pos) {
			pos.x = pos.x + x;
			pos.y = pos.y + y;
		}

	}

}
