package game_logic.gui.controllers.selectors;

import org.joml.Vector2f;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.storage.Meshes;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;

public class CombatSelector implements TileObject {

	//	Tile & map
	private Tile homeTile;
	private Map homeMap;
	
	//	Decoration
	private Decoration decoration;
	
	public CombatSelector() {
		decoration = new Decoration(Meshes.COMBAT_SELECTOR, this);
		decoration.setScale(0.4f);
	}

	@Override
	public String getName() {
		return "Destination selector";
	}

	@Override
	public Decoration[] getDecorations() {
		return new Decoration[] {decoration};
	}

	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return true;
	}

	@Override
	public void updateGraphics() {
	}

	@Override
	public void turn_started() {
	}

	@Override
	public void registerToMap(Map map) {
		homeMap = map;
	}

	@Override
	public void deleteFromMap(Map map) {
		homeMap = null;
	}

	@Override
	public void registerToTile(Tile homeTile) {
		this.homeTile = homeTile;
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		this.homeTile = null;
	}

	@Override
	public Vector2f getDirection() {
		return new Vector2f(0, 1);
	}

	@Override
	public void turn_ended() {
	}

	@Override
	public Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public Map getHomeMap() {
		return homeMap;
	}

	@Override
	public String getObjectInformation() {
		return null;
	}
	
	@Override
	public float getDecorationHeight() {
		return 11;
	}

	@Override
	public boolean staticVisibility() {
		return true;
	}


}
