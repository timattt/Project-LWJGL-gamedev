package resources;

import java.util.LinkedList;

public final class ResourcesPack {

	// Resources
	private final LinkedList<Resource> resources = new LinkedList<Resource>();
	
	// Sealed
	private boolean sealed = false;
	
	public ResourcesPack() {
	}
	
	public void put(Resource res) throws Exception {
		if (sealed) {
			throw new Exception("Already sealed!");
		}
		resources.add(res);
	}
	
	public void cleanup() throws Exception {
		if (!sealed) {
			throw new Exception("Not yet sealed!");
		}
		
		for (Resource res : resources) {
			res.cleanup();
		}
	}
	
	public void seal() {
		sealed = true;
	}
	
	public static final void cleanupPacks(ResourcesPack[] packs) throws Exception {
		for (ResourcesPack pack : packs) {
			pack.cleanup();
		}
	}
	
}
