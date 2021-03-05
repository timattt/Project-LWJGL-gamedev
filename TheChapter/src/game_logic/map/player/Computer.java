package game_logic.map.player;

import java.util.LinkedList;

import org.joml.Vector2i;

import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.unit.MilitaryUnit;
import game_logic.map.unit.Unit;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Controllable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;
import game_logic.tile_object.moving.RouteCreator.RouteCreationStatus;

public class Computer extends Player {

	public Computer() {
	}

	@Override
	public void doTurn(Team team) {
		Map map = team.getMap();

		for (Controllable obj : team.getControllableObjects()) {
			if (!(obj instanceof Unit)) {
				continue;
			}
			Unit unit = (Unit) obj;
			if (unit.getCurrentHealth() <= 0) {
				continue;
			}
			if (!(unit instanceof Combatable)) {
				continue;
			}
			Combatable enemy = pickEnemy((MilitaryUnit) unit, team);
			if (enemy == null) {
				return;
			}

			while (unit.getCurrentMovePoints() > 0) {
				if (Combatable.canAttack(unit, (TileObject) enemy)) {
					((Combatable) unit).doCombat((Combatable) enemy);
					break;
				}

				Vector2i end = new Vector2i(map.getCoordinates(((TileObject) enemy).getHomeTile()));

				Tile[] route = Movable.findBestRoute(unit, end, new RouteCreationStatus());

				if (route.length < 2) {
					break;
				}

				end = unit.getHomeMap().getCoordinates(route[1]);

				RouteCreationStatus st = new RouteCreationStatus();
				map.moveObject(unit, end, st);

				if (st.status == RouteCreationStatus.status_blocked
						&& !Combatable.canAttack(unit, (TileObject) enemy)) {
					break;
				}
			}
		}

	}

	private Combatable pickEnemy(MilitaryUnit u, Team team) {
		Map map = team.getMap();
		LinkedList<Combatable> all_units = new LinkedList<Combatable>();
		for (int x = 0; x < team.getMap().getTiles().length; x++) {
			for (int y = 0; y < team.getMap().getTiles()[x].length; y++) {
				for (Combatable o : team.getMap().getTiles()[x][y].getAll(Combatable.class)) {
					if (u.isEnemy((Combatable) o)) {
						all_units.add((Combatable) o);
					}
				}
			}
		}

		LinkedList<Combatable> nearest_unit = new LinkedList<Combatable>();
		float nearest_range = Float.MAX_VALUE;
		for (Combatable unit : all_units) {
			if (((Combatable) u).isEnemy((Combatable) unit)
					&& map.getLength(u.getHomeTile(), ((TileObject) unit).getHomeTile()) <= nearest_range) {
				nearest_range = map.getLength(u.getHomeTile(), ((TileObject) unit).getHomeTile());
			}
		}

		for (Combatable unit : all_units) {
			if (((Combatable) u).isEnemy((Combatable) unit)
					&& map.getLength(u.getHomeTile(), ((TileObject) unit).getHomeTile()) <= nearest_range) {
				nearest_unit.add(unit);
			}
		}

		return nearest_unit.isEmpty() ? null : nearest_unit.get((int) (Math.random() * nearest_unit.size()));
	}

}
