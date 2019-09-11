/**
 * 
 */
package game_logic.map;

import engine.Engine;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import game_logic.GameOptions;

/**
 * @author timat
 *
 */
@MonoDemeanor
public class TileSizeHandler {

	@MonoDemeanorInstance
	public static final TileSizeHandler instance = new TileSizeHandler();

	// Tile cuts quantity
	private final int tileCuts;

	// Size
	private final float tileSize = 6f;
	private final float tileObjectStandartSize;
	private final float tileSubquadSide;

	// Storage data into arrays
	private final int tileVerticesComponentsQuantity;
	private final int tileTextureComponentsQuantity;

	// Quantity of vertices in tile
	private final int tileTotalVertices;
	
	/**
	 * 
	 */
	private TileSizeHandler() {
		tileCuts = ((GameOptions) Engine.getExternalOptions()).getTile_cuts();

		tileObjectStandartSize = tileSize * 2f / 3f;
		tileSubquadSide = tileSize / (tileCuts + 1f);

		tileVerticesComponentsQuantity = 3 * 6 * (tileCuts + 1) * (tileCuts + 1);
		tileTextureComponentsQuantity = tileVerticesComponentsQuantity / 3 * 2;
		
		tileTotalVertices = (int) Math.pow(tileCuts + 2, 2);
	}

	public final int getTileCuts() {
		return tileCuts;
	}

	public final float getTileSize() {
		return tileSize;
	}

	public final float getTileObjectStandartSize() {
		return tileObjectStandartSize;
	}

	public final float getTileSubquadSide() {
		return tileSubquadSide;
	}

	public final int getTileVerticesComponentsQuantity() {
		return tileVerticesComponentsQuantity;
	}

	public final int getTileTextureComponentsQuantity() {
		return tileTextureComponentsQuantity;
	}

	public final int getTileTotalVertices() {
		return tileTotalVertices;
	}

}
