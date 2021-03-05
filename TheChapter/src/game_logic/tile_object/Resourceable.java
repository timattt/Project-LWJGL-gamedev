package game_logic.tile_object;

import game_logic.map.resources.Resource;

public interface Resourceable {

	public ResourceHeap getResources();

	public abstract static class ResourceHeap {
		private Resource[] resources;
		private int[] quantities;

		private int index = 0;

		public ResourceHeap(int quantity) {
			resources = new Resource[quantity];
			quantities = new int[quantity];
			fill();
		}

		protected void put(Resource res, int quant) {
			resources[index] = res;
			quantities[index] = quant;
			index++;
		}

		protected void flip() {
			index = 0;
		}

		public final Resource[] getResources() {
			return resources;
		}

		public final int[] getQuantities() {
			return quantities;
		}

		public void resize(int size) {
			resources = new Resource[size];
			quantities = new int[size];
		}

		public void clear() {
			resources = new Resource[0];
			quantities = new int[0];
		}

		public final void merge(ResourceHeap heap) {
			A: for (int i = 0; i < heap.resources.length; i++) {
				Resource res = heap.resources[i];
				int quant = heap.quantities[i];

				for (int a = 0; a < resources.length; a++) {
					if (resources[a] == res) {
						quantities[a] += quant;
						continue A;
					}
				}

				Resource[] new_heap = new Resource[resources.length + 1];
				int[] new_quants = new int[quantities.length + 1];
				for (int a = 0; a < resources.length; a++) {
					new_heap[a] = resources[a];
					new_quants[a] = quantities[a];
				}
				new_quants[quantities.length] = quant;
				new_heap[resources.length] = res;

				resources = new_heap;
				quantities = new_quants;
			}
		}

		public int getResourceQuanity(Resource res) {
			for (int i = 0; i < resources.length; i++) {
				if (resources[i] == res) {
					return quantities[i];
				}
			}

			return 0;
		}

		protected abstract void fill();
	}
}
