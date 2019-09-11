/**
 * 
 */
package scenario.stuf;

import game_logic.map.settlement.Building;
import graphicsSupport.mesh.Mesh;
import scenario.storage.Meshes;
import scenario.storage.Resources;

/**
 * @author timat
 *
 */
public class House extends Building {

	private static final ResourceHeap heap = new ResourceHeap(2) {
		@Override
		protected void fill() {
			put(Resources.FOOD, 1);
			put(Resources.TOOLS, 1);
		}
	};

	/**
	 * 
	 */
	public House() {
		setScalarCoordinates(0.5f, 0.5f);
	}

	@Override
	public String getName() {
		return "House";
	}

	@Override
	public float getDecorationHeight() {
		return 14f;
	}

	@Override
	public ResourceHeap getResources() {
		return heap;
	}

	@Override
	public Mesh[][] getMeshes() {
		return new Mesh[][] { Meshes.HOUSE };
	}

	@Override
	public int getBaseCost() {
		return 30;
	}

}
