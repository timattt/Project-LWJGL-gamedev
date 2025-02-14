package game_logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import engine.options.Options;

public class GameOptions extends Options {

	private boolean map_grid = true;
	private float cameraFromGround = 5f;
	private boolean camera_bounded_to_terrain = false;
	private boolean day_night_cycle_enabled = true;
	private int tile_cuts = 1;
	private boolean disableVisibility = false;
	private boolean areUnitsOrthogonalToTiles = false;
	private boolean flatternTerrain = false;
	private boolean useTextureForPlains = false;
	
	public GameOptions() {
	}

	@Override
	public void createNewOptionsFile() throws IOException {
		if (dir.exists()) {
			dir.delete();
		}

		dir.createNewFile();

		BufferedWriter writer = new BufferedWriter(new FileWriter(dir));

		writer.write("map grid enabled:" + Boolean.toString(this.map_grid));
		writer.newLine();

		writer.write("camera from ground length:" + Float.toString(cameraFromGround));
		writer.newLine();

		writer.write("camera bounded to terrain:" + Boolean.toString(this.camera_bounded_to_terrain));
		writer.newLine();

		writer.write("day night cycle enabled:" + Boolean.toString(this.day_night_cycle_enabled));
		writer.newLine();

		writer.write("tile cuts:" + Integer.toString(tile_cuts));
		writer.newLine();
		
		writer.write("visibility disabled:" + Boolean.toString(disableVisibility));
		writer.newLine();
		
		writer.write("units orthogonal to tiles:" + Boolean.toString(areUnitsOrthogonalToTiles));
		writer.newLine();
		
		writer.write("flattern terrain:" + Boolean.toString(flatternTerrain));
		writer.newLine();
		
		writer.write("use texture for plains:" + Boolean.toString(useTextureForPlains));
		writer.newLine();

		writer.flush();
		writer.close();

	}

	@Override
	public void loadOptions() throws NumberFormatException, IOException {
		if (!dir.exists()) {
			createNewOptionsFile();
		} else {
			BufferedReader reader = new BufferedReader(new FileReader(dir));

			map_grid = Boolean.parseBoolean(cutComments(reader.readLine()));
			cameraFromGround = Float.parseFloat(cutComments(reader.readLine()));
			camera_bounded_to_terrain = Boolean.parseBoolean(cutComments(reader.readLine()));
			day_night_cycle_enabled = Boolean.parseBoolean(cutComments(reader.readLine()));
			tile_cuts = Integer.parseInt(cutComments(reader.readLine()));
			disableVisibility = Boolean.parseBoolean(cutComments(reader.readLine()));
			areUnitsOrthogonalToTiles = Boolean.parseBoolean(cutComments(reader.readLine()));
			flatternTerrain = Boolean.parseBoolean(cutComments(reader.readLine()));
			useTextureForPlains = Boolean.parseBoolean(cutComments(reader.readLine()));
			
			reader.close();
		}
	}

	public boolean isFlatternTerrain() {
		return flatternTerrain;
	}

	public final boolean isMap_grid() {
		return map_grid;
	}

	public final float getCameraFromGround() {
		return cameraFromGround;
	}

	public final boolean isCamera_bounded_to_terrain() {
		return camera_bounded_to_terrain;
	}

	public final boolean isDay_night_cycle_enabled() {
		return day_night_cycle_enabled;
	}

	public final void setMap_grid(boolean map_grid) {
		this.map_grid = map_grid;
	}

	public final void setCameraFromGround(float cameraFromGround) {
		this.cameraFromGround = cameraFromGround;
	}

	public final void setCamera_bounded_to_terrain(boolean camera_bounded_to_terrain) {
		this.camera_bounded_to_terrain = camera_bounded_to_terrain;
	}

	public final void setDay_night_cycle_enabled(boolean day_night_cycle_enabled) {
		this.day_night_cycle_enabled = day_night_cycle_enabled;
	}

	public boolean isUseTextureForPlains() {
		return useTextureForPlains;
	}

	public final int getTile_cuts() {
		return tile_cuts;
	}

	public boolean isAreUnitsOrthogonalToTiles() {
		return areUnitsOrthogonalToTiles;
	}

	public final boolean isDisableVisibility() {
		return disableVisibility;
	}



}
