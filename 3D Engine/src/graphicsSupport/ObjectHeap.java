/**
 * 
 */
package graphicsSupport;

import java.util.Vector;

/**
 * @author timat
 *
 */
public class ObjectHeap {

	private final Vector<Item> items = new Vector<Item>();
	private final int id;

	private ObjectHeap(int id) {
		super();
		this.id = id;
	}

	public final Vector<Item> getItems() {
		return items;
	}

	public final int getId() {
		return id;
	}

}
