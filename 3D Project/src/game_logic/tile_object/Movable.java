package game_logic.tile_object;

import java.util.LinkedList;

import org.joml.Vector2i;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.tile_object.moving.RouteCreator;
import game_logic.tile_object.moving.RouteCreator.RouteCreationStatus;

public interface Movable {
	public void move(Tile[] path);

	public RouteCreator[] getRouteCreatorAlgorithm();

	public int getMovePoints();

	public int getCurrentMovePoints();

	public void stop();

	public static <T extends TileObject & Movable> Tile[] findBestRoute(T obj, Vector2i dest,
			RouteCreationStatus status) {
		Map map = obj.getHomeMap();

		// Collecting all variants of route
		RouteCreator[] algs = obj.getRouteCreatorAlgorithm();
		Tile[][] paths = new Tile[algs.length][];
		for (int i = 0; i < algs.length; i++) {
			paths[i] = algs[i].createRoute(obj, map.getCoordinates(obj.getHomeTile()), dest, status);
		}
		
		// Finding best
		// Finding paths that reaches destination
		LinkedList<Tile[]> tiles_reach = new LinkedList<Tile[]>();
		for (int i = 0; i < paths.length; i++) {
			if (paths[i][paths[i].length - 1] == map.getTile(dest)) {
				tiles_reach.add(paths[i]);
			}
		}
		
		// If any path reaches destination then get the biggest
		int best_i = -1;
		int best;
		if (tiles_reach.size() != 0) {
			best = Integer.MAX_VALUE;
			for (int i = 0; i < tiles_reach.size(); i++) {
				if (tiles_reach.get(i).length < best) {
					best = tiles_reach.get(i).length;
					best_i = i;
				}
			}
			return best_i != -1 ? tiles_reach.get(best_i) : new Tile[0];
		}
		// Else we find the closest to destination from everyone
		else {
			best = Integer.MAX_VALUE;
			for (int i = 0; i < paths.length; i++) {
				int length = map.getLength(paths[i][paths[i].length - 1], map.getTile(dest));
				if (length < best) {
					best = length;
					best_i = i;
				}
			}
			return best_i != -1 ? paths[best_i] : new Tile[0];
		}
	}
	
	public default boolean hasFullMovePoints() {
		return getCurrentMovePoints() == getMovePoints();
	}
	
}