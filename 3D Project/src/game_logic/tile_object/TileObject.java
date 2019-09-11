package game_logic.tile_object;

import org.joml.Vector2f;

import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;

public interface TileObject {

	/**
	 * This is the base direction, object rotation angle calculates from this
	 * and object direction.
	 */
	public static final Vector2f BASE_DIRECTION = new Vector2f(0, 1);

	/**
	 * This method is invoked when object is deleting from its map.
	 * 
	 * @param map
	 *            : Map which object will be delete from.
	 */
	public void deleteFromMap(Map map);

	/**
	 * This method gives all decorations that needs to be renderer.
	 * 
	 * @return : Array of decorations.
	 */
	public Decoration[] getDecorations();

	/**
	 * This method gives objects tile map direction. Must be stored in objects
	 * memory.
	 * 
	 * @return : Direction as a vector.
	 */
	public Vector2f getDirection();

	/**
	 * This method gives map that have object in itself.
	 * 
	 * @return : Objects home map. Must be stored in objects memory.
	 */
	public Map getHomeMap();

	/**
	 * This method gives tile that have object in itself.
	 * 
	 * @return : Objects home tile. Must be stored in objects memory.
	 */
	public Tile getHomeTile();

	/**
	 * This method gives object's main name.
	 * 
	 * @return : Name as string.
	 */
	public String getName();

	/**
	 * This method gives information that will be written on the tile objects
	 * information panel.
	 * 
	 * @return : Object information as string.
	 */
	public String getObjectInformation();

	/**
	 * This parameter tells if some object (like units) can move on same tile
	 * with this object.
	 * 
	 * @param obj
	 *            : Object that wants to walk on.
	 * @return : True if can be walked on.
	 */
	public <T extends Movable & TileObject> boolean isWalkable(T obj);

	/**
	 * This method is invoked when object is placed on a map. This map must be
	 * saved in object memory.
	 * 
	 * @param map
	 *            : Map which object will be on.
	 */
	public void registerToMap(Map map);

	/**
	 * This method is invoked when object is registering to tile. This tile must
	 * be saved in objects memory.
	 * 
	 * @param homeTile
	 *            : Tile to register to.
	 */
	public void registerToTile(Tile homeTile);

	/**
	 * This method is invoked when object deleting from its tile.
	 * 
	 * @param homeTile
	 *            : Tile to delete from.
	 */
	public void removeFromTile(Tile homeTile);

	/**
	 * This method is invoked every time when the turn is ended.
	 */
	public void turn_ended();

	/**
	 * This method is invoked every time when the new turn is started.
	 */
	public void turn_started();

	/**
	 * This method is invoked every time the game is updated.
	 */
	public void updateGraphics();
	 
	/**
	 * This method gives decoration size as it in the model.
	 * @return : Size.
	 */
	public float getDecorationHeight();

	/**
	 * This method tells will the object will be visible in invisible team mode. 
	 * @return
	 */
	public boolean staticVisibility();
	
	/**
	 * This method must this object be prepared when registering to map.
	 * @return
	 */
	public default boolean mustBePrepared() {
		return true;
	}
}
