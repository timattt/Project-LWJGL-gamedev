package game_logic.map;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.joml.Vector2i;

public abstract class MapGenerator {

	// Name
	public final String name;

	public MapGenerator(String name) {
		this.name = name;
	}

	/**
	 * This method will be always invoked when generator starts creating new map.
	 */
	public abstract void startGen();

	/**
	 * This method will be always invoked when generator finishes creating map.
	 */
	public abstract void endGen();

	public abstract String getMapName();

	public abstract Vector2i getMapSize();

	public abstract Tile[][] getMapTiles();

	public abstract LinkedList<Team> getTeams();

	public abstract void putObjects(Map map);
	
	public abstract float getMaxHeight();

	protected final float[][] readHeightMap(File file, int width, int height)
			throws IOException {
		BufferedImage map = ImageIO.read(file);

		float[][] out = new float[width][height];

		int w = map.getWidth();
		int h = map.getHeight();

		int stepX = w / width;
		int stepY = h / height;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color col = new Color(map.getRGB(stepX * x, stepY * y));

				float val = (float) (col.getRed() + col.getGreen() + col.getBlue()) / (256f * 3f) * getMaxHeight();
				
				out[x][y] = val;
				
			}
		}
		
		map.flush();
		return out;
	}

	protected final Tile[][] genTiles(float[][] mapZ) {
		Tile[][] result = new Tile[(mapZ.length - 1) / (TileSizeHandler.instance.getTileCuts() + 1)][(mapZ[0].length - 1) / (TileSizeHandler.instance.getTileCuts() + 1)];

		for (int tile_x = 0; tile_x < result.length; tile_x++) {
			for (int tile_z = 0; tile_z < result[0].length; tile_z++) {
				result[tile_x][tile_z] = new Tile(mapZ, tile_x * (TileSizeHandler.instance.getTileCuts() + 1),
						tile_z * (TileSizeHandler.instance.getTileCuts() + 1));
			}
		}

		return result;
	}

}
