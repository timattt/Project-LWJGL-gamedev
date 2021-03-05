package game_logic.map.terrain;

import game_logic.map.Tile;
import graphicsSupport.mesh.Mesh;

public class Plains extends Terrain {

	public Plains() {
	}

	@Override
	public Mesh[][] get_meshes() {
		return new Mesh[][] {};
	}

	@Override
	public void registerToTile(Tile homeTile) {
		super.registerToTile(homeTile);
		//homeMap.setTextureToTileMapMesh(Textures.PLAINS_TERRAIN, homeTile);
	}

	@Override
	public String getName() {
		return "Plains";
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		//homeMap.removeTextureFromTileMapMesh(Textures.PLAINS_TERRAIN, this.homeTile);
		super.removeFromTile(homeTile);
	}

	@Override
	public float getDecorationHeight() {
		return 4;
	}

	@Override
	public boolean staticVisibility() {
		return true;
	}


}
