package game_logic.map.settlement;

import org.joml.Vector2f;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;
import graphicsSupport.mesh.Mesh;

public class BuildingProcessMark implements TileObject {

	// Home
	private Tile homeTile;
	private Map homeMap;
	
	// Decorations
	private Decoration[] decorations;
	private float height;
	
	public BuildingProcessMark(Mesh[][] meshes, float h) {
		decorations = new Decoration[meshes.length];
		for (int i = 0; i < meshes.length; i++) {
			decorations[i] = new Decoration(meshes[i], this);
		}
	this.height = h;
	}

	@Override
	public void deleteFromMap(Map map) {
		homeMap = null;
	}

	@Override
	public Decoration[] getDecorations() {
		return decorations;
	}

	@Override
	public Vector2f getDirection() {
		return BASE_DIRECTION;
	}

	@Override
	public Map getHomeMap() {
		return homeMap;
	}

	@Override
	public Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public String getName() {
		return "BuildingProcessMark";
	}

	@Override
	public String getObjectInformation() {
		return null;
	}

	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return true;
	}

	@Override
	public void registerToMap(Map map) {
		homeMap = map;
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
	public void turn_ended() {
	}

	@Override
	public void turn_started() {
	}

	@Override
	public void updateGraphics() {
	}

	@Override
	public float getDecorationHeight() {
		return height;
	}

	@Override
	public boolean staticVisibility() {
		return false;
	}

}
