package game_logic.tile_object.moving;

import java.util.LinkedList;

import org.joml.Vector2i;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;

public class LinearRouteCreator extends RouteCreator {

	public LinearRouteCreator() {
	}

	@Override
	public <T extends TileObject & Movable> Tile[] createRoute(T obj, Vector2i start, Vector2i end,
			RouteCreationStatus status) {
		Vector2i direction = new Vector2i();
		Map map = obj.getHomeMap();

		// Finding paths
		end.add(-start.x, -start.y, direction);
		Vector2i[] paths = decomposeDirection(direction);

		LinkedList<Tile> result = new LinkedList<Tile>();

		result.addFirst(map.getTile(start));

		Vector2i tile_pos = new Vector2i(start);
		for (int i = 0; i < paths.length; i++) {
			tile_pos.add(paths[i]);
			Tile t = map.getTile(tile_pos);
			if (!t.isWalkable(obj)) {
				status.status = RouteCreationStatus.status_blocked;
				break;
			}
			result.add(t);
		}
		Tile[] res_arr = new Tile[result.size()];
		for (int i = 0; i < result.size(); i++) {
			res_arr[i] = result.get(i);
		}
		return res_arr;
	}

}
