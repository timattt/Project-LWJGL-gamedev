package scenario.stuf;

import org.joml.Vector2f;

import game_logic.map.Tile;
import game_logic.map.decoration.Decoration;
import game_logic.map.terrain.Terrain;
import game_logic.storage.SoundConfigs;
import game_logic.tile_object.Resourceable;
import graphicsSupport.mesh.Mesh;
import scenario.storage.Meshes;
import scenario.storage.Resources;
import scenario.storage.Sounds;
import soundSupport.SoundManager;
import soundSupport.SoundSource;

public class Forest extends Terrain implements Resourceable {

	// Sound
	private SoundSource soundSource;

	// Trees number
	public static final Vector2f[] trees_positions = new Vector2f[] { new Vector2f(0.75f, 0.75f),
			new Vector2f(0.75f, 0.25f), new Vector2f(0.75f, -0.25f), new Vector2f(0.75f, -0.75f),

			new Vector2f(0.25f, 0.75f), new Vector2f(0.25f, 0.25f), new Vector2f(0.25f, -0.25f),
			new Vector2f(0.25f, -0.75f),

			new Vector2f(-0.25f, 0.75f), new Vector2f(-0.25f, 0.25f), new Vector2f(-0.25f, -0.25f),
			new Vector2f(-0.25f, -0.75f),

			new Vector2f(-0.75f, 0.75f), new Vector2f(-0.75f, 0.25f), new Vector2f(-0.75f, -0.25f),
			new Vector2f(-0.75f, -0.75f), };

	// Resources
	private static final ResourceHeap heap = new ResourceHeap(1) {

		@Override
		protected void fill() {
			put(Resources.WOOD, 3);
		}

	};

	public Forest() {
		super();
		setWalkable(false);

		decorations = new Decoration[16];
		for (int i = 0; i < decorations.length; i++) {
			decorations[i] = new Decoration(Meshes.TREE, this);
			decorations[i].scalar_location
					.set(new Vector2f((float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f));
		}

	}

	@Override
	public void removeFromTile(Tile homeTile) {
		super.removeFromTile(homeTile);
		if (soundSource != null) {
			soundSource.stop();
		}
	}

	@Override
	public String getName() {
		return "Forest";
	}

	@Override
	public Mesh[][] get_meshes() {
		return new Mesh[][] {};
	}

	@Override
	public float getDecorationHeight() {
		return 22f;
	}

	@Override
	public void updateGraphics() {
		if (soundSource == null) {
			soundSource = SoundManager.instance.addSoundToPlay(this.homeTile.center, 1f, SoundConfigs.CONFIG2,
					System.currentTimeMillis(), System.currentTimeMillis(), Sounds.FOREST_AMBIENCE);
		}
	}

	@Override
	public ResourceHeap getResources() {
		return heap;
	}

	@Override
	public boolean staticVisibility() {
		return true;
	}

}
