package scenario;

import java.io.IOException;
import java.util.LinkedList;

import org.joml.Vector2i;

import engine.Engine;
import game_logic.map.Map;
import game_logic.map.MapGenerator;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.TileSizeHandler;
import game_logic.map.player.Computer;
import game_logic.map.settlement.Settlement;
import game_logic.map.terrain.Plains;
import game_logic.map.unit.Unit;
import scenario.stuf.Forest;
import scenario.stuf.LineInfantry;

public class TestMapGenerator extends MapGenerator {

	// Size
	private static final Vector2i size = new Vector2i(30, 30);

	private LinkedList<Team> teams;

	public TestMapGenerator() {
		super("TestScenarioMapGen");
	}

	@Override
	public void startGen() {
		teams = new LinkedList<Team>();
	}

	@Override
	public void endGen() {
	}

	@Override
	public String getMapName() {
		return "TestMap";
	}

	@Override
	public Tile[][] getMapTiles() {
		try {
			return genTiles(
					readHeightMap(Engine.getResourceAsFile("/scenarios/TestScenario/Resources/textures/HeightMap.png"),
							(TileSizeHandler.instance.getTileCuts() + 1) * size.x + 1,
							(TileSizeHandler.instance.getTileCuts() + 1) * size.y + 1));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public LinkedList<Team> getTeams() {
		teams.clear();
		teams.add(new Team());
		teams.getLast().addPlayerInTeam(new Computer());
		teams.add(new Team());
		teams.getLast().addPlayerInTeam(new Computer());
		return teams;
	}

	@Override
	public void putObjects(Map map) {
		for (int x = 0; x < size.x; x++) {
			for (int y = 0; y < size.y; y++) {
				map.registerTileObject(new Plains(), x, y);
			}
		}

		for (int i = 0; i < 1; i++)
			map.registerTileObject(new LineInfantry(), 10 + i, 10, teams.getFirst());

		for (int i = 0; i < 1; i++)
			map.registerTileObject(new LineInfantry(), 10 + i, 13, teams.getLast());

		// Trees
		for (int i = 0; i < 400; i++) {
			int x;
			int y;
			do {
				x = (int) (Math.random() * (size.x));
				y = (int) (Math.random() * (size.y));
			} while (map.getTile(x, y).containsType(Forest.class) || map.getTile(x, y).containsType(Unit.class)
					|| map.getTile(x, y).containsType(Settlement.class));
			map.registerTileObject(new Forest(), x, y);
		}

	}

	@Override
	public Vector2i getMapSize() {
		return size;
	}

	@Override
	public float getMaxHeight() {
		return 20f;
	}

}
