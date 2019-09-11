package game_logic.map.terrain;

import org.joml.Vector2f;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;
import graphicsSupport.mesh.Mesh;

public abstract class Terrain implements TileObject {

	// Home
	protected Map homeMap;
	protected Tile homeTile;

	// Direction
	private static final Vector2f direction = new Vector2f(0, 1);

	// Constants
	public static final float SCALE = 1f;

	// Parameters
	protected boolean walkable = true;

	// Decorations
	protected Decoration[] decorations;
	
	public Terrain() {
		decorations = prepareDecorations(get_meshes());
	}

	protected Decoration[] prepareDecorations(Mesh[][] meshes) {
		Decoration[] decs = new Decoration[meshes.length];
		for (int i = 0; i < meshes.length; i++) {
			decs[i] = new Decoration(meshes[i], this);
		}
		return decs;
	}

	@Override
	public Vector2f getDirection() {
		return direction;
	}

	@Override
	public void updateGraphics() {
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
	public void registerToMap(Map map) {
		this.homeMap = map;
	}

	@Override
	public void deleteFromMap(Map map) {
		this.homeMap = null;
	}

	@Override
	public <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return walkable;
	}

	public final void setWalkable(boolean walkable) {
		this.walkable = walkable;
	}

	public abstract Mesh[][] get_meshes();

	@Override
	public Decoration[] getDecorations() {
		return decorations;
	}

	@Override
	public void turn_ended() {
	}

	@Override
	public void turn_started() {
	}

	@Override
	public Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public Map getHomeMap() {
		return homeMap;
	}

	protected void addDecoration(Decoration new_dec) {
		Decoration[] new_decs = new Decoration[decorations.length + 1];
		int i = 0;
		for (Decoration dec : decorations) {
			new_decs[i] = dec;
			i++;
		}
		new_decs[decorations.length] = new_dec;

		decorations = new_decs;
	}

	@Override
	public String getObjectInformation() {
		return "Terrain: " + getName();
	}
}
