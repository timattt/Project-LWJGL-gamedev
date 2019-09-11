package game_logic.map.resources;

import graphicsSupport.texture.Texture;

public class Resource {
	
	// Parameters
	public final String name;
	public final Texture texture;

	public Resource(String name, Texture texture) {
		super();
		this.name = name;
		this.texture = texture;
	}
	
}
