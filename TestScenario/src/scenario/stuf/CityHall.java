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
public class CityHall extends Building {

	private static final ResourceHeap heap = new ResourceHeap(2) {
		@Override
		protected void fill() {
			put(Resources.FOOD, 2);
			put(Resources.TOOLS, 1);
		}
	};

	/**
	 * 
	 */
	public CityHall() {
	}

	@Override
	public String getName() {
		return "City hall";
	}

	@Override
	public float getDecorationHeight() {
		return 10f;
	}

	@Override
	public ResourceHeap getResources() {
		return heap;
	}

	@Override
	public Mesh[][] getMeshes() {
		return new Mesh[][] { Meshes.CITY_HALL };
	}

	@Override
	public int getBaseCost() {
		return 30;
	}

}
