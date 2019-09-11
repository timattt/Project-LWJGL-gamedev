package graphicsSupport.gui.components;

import org.joml.Vector2f;

public interface VgComponent {
	public void render();
	public boolean isInComponent(Vector2f vector2d);
	public boolean update();
}
