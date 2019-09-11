package game_logic.map.settlement;

import org.joml.Vector2f;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.tile_object.BuildableByCity;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Resourceable;
import game_logic.tile_object.TileObject;
import graphicsSupport.mesh.Mesh;

public abstract class Building implements TileObject, Resourceable, BuildableByCity {

	// Decorations
	private Decoration[] decorations;

	// Home
	private Tile homeTile;
	private Map homeMap;

	// Scalar coordinates
	private Vector2f scalarCoords = new Vector2f();
	
	public Building() {
		Mesh[][] meshes = getMeshes();
		decorations = new Decoration[meshes.length];
		for (int i = 0; i < meshes.length; i++) {
			decorations[i] = new Decoration(meshes[i], this);
		}
	}

	public abstract Mesh[][] getMeshes();

	public abstract int getBaseCost();
	
	@Override
	public void deleteFromMap(Map map) {
		map = null;
	}

	public void setScalarCoordinates(float x, float y) {
		scalarCoords.set(x, y);
		for (Decoration dec : decorations) {
			dec.scalar_location.set(x, y);
		}
	}
	
	public Vector2f getScalarCoordinates() {
		return scalarCoords;
	}
	
	@Override
	public Decoration[] getDecorations() {
		return decorations;
	}

	@Override
	public Vector2f getDirection() {
		return TileObject.BASE_DIRECTION;
	}

	@Override
	public String getObjectInformation() {
		return "Building " + getName();
	}

	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return true;
	}

	@Override
	public void registerToMap(Map map) {
		this.homeMap = map;
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
	public Map getHomeMap() {
		return homeMap;
	}

	@Override
	public Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public final boolean staticVisibility() {
		return true;
	}

}
