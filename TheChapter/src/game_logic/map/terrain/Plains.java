package game_logic.map.terrain;

import engine.Engine;
import game_logic.GameOptions;
import game_logic.map.Tile;
import game_logic.storage.Textures;
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
		if (((GameOptions) Engine.getExternalOptions()).isUseTextureForPlains()) {
			homeMap.setTextureToTileMapMesh(Textures.PLAINS_TERRAIN, homeTile);
		}
	}

	@Override
	public String getName() {
		return "Plains";
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		if (((GameOptions) Engine.getExternalOptions()).isUseTextureForPlains()) {
			homeMap.removeTextureFromTileMapMesh(Textures.PLAINS_TERRAIN, this.homeTile);
		}
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
